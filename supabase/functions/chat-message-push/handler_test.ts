import { createHandler, type Ports, type Target } from "./handler.ts";
function assert(ok: unknown) { if (!ok) throw new Error("Assertion failed"); }
const secret = "synthetic-test-secret-with-at-least-32-characters";
const messageId = "00000000-0000-0000-0000-000000000001";
const target: Target = {
  device_id: "device", recipient_id: "recipient", token: "synthetic",
  conversation_id: "conversation", title: "Dịu", body: "Bạn có tin nhắn mới", mood: "thuong",
};
function fixture() {
  let list = [target], sent = 0, removed = 0, claims = 0;
  let leased = false;
  let outcome: "accepted" | "unregistered" | "error" = "accepted";
  const receipts: boolean[] = [];
  const ports: Ports = {
    targets() { return Promise.resolve(list); },
    claim() { claims++; if (leased) return Promise.resolve(null); leased = true; return Promise.resolve("lease"); },
    finish(_m, _d, _l, accepted) {
      receipts.push(accepted);
      if (!accepted) leased = false;
      return Promise.resolve();
    },
    send() { sent++; if (outcome === "error") return Promise.reject(new Error("network")); return Promise.resolve(outcome); },
    removeExpired() { removed++; return Promise.resolve(); },
  };
  return {
    ports, handler: createHandler(secret, ports), receipts,
    empty: () => { list = []; },
    outcome: (value: typeof outcome) => { outcome = value; },
    stats: () => ({ sent, removed, claims }),
  };
}
function request(body: unknown = {
  type: "INSERT", schema: "public", table: "messages", record: { id: messageId },
}, auth = secret) {
  return new Request("https://example.test", {
    method: "POST", headers: { "x-chat-push-secret": auth },
    body: JSON.stringify(body),
  });
}
Deno.test("unauthorized callers cannot touch database or FCM", async () => {
  const f = fixture();
  assert((await f.handler(request(undefined, "wrong"))).status === 401);
  assert(f.stats().claims === 0 && f.stats().sent === 0);
});
Deno.test("reject non-INSERT events, wrong tables and malformed IDs", async () => {
  const f = fixture();
  for (const body of [
    { type: "UPDATE" },
    { type: "INSERT", schema: "public", table: "push_tokens", record: { id: messageId } },
    { type: "INSERT", schema: "public", table: "messages", record: { id: "invalid" } },
    null,
  ]) assert((await f.handler(request(body))).status === 400);
});
Deno.test("valid event sends once; replay and concurrent invocation are suppressed", async () => {
  const f = fixture();
  const results = await Promise.all([f.handler(request()), f.handler(request())]);
  assert(results.every((result) => result.status === 200));
  assert(f.stats().sent === 1);
  await f.handler(request());
  assert(f.stats().sent === 1);
});
Deno.test("no eligible targets means no FCM calls", async () => {
  const f = fixture(); f.empty();
  assert((await f.handler(request())).status === 200);
  assert(f.stats().sent === 0);
});
Deno.test("recheck eligibility after claim prevents a revoked recipient from receiving", async () => {
  const f = fixture();
  f.ports.claim = () => { f.empty(); return Promise.resolve("lease"); };
  await f.handler(request());
  assert(f.stats().sent === 0 && f.receipts[0] === true);
});
Deno.test("network failure returns 503; explicit replay can retry", async () => {
  const f = fixture(); f.outcome("error");
  assert((await f.handler(request())).status === 503);
  assert(f.receipts[0] === false);
  f.outcome("accepted");
  assert((await f.handler(request())).status === 200);
  assert(f.stats().sent === 2);
});
Deno.test("unregistered endpoint is cleaned up and not retried", async () => {
  const f = fixture(); f.outcome("unregistered");
  await f.handler(request());
  await f.handler(request());
  assert(f.stats().removed === 1 && f.stats().sent === 1);
});
Deno.test("accepted FCM with receipt failure is not immediately marked failed", async () => {
  const f = fixture();
  f.ports.finish = () => Promise.reject(new Error("database unavailable"));
  assert((await f.handler(request())).status === 503);
  assert(f.stats().sent === 1 && f.receipts.length === 0);
});
Deno.test("database failure is sanitized", async () => {
  const f = fixture();
  f.ports.targets = () => Promise.reject(new Error("private-token-value"));
  const response = await f.handler(request());
  assert(response.status === 503 && !(await response.text()).includes("private-token-value"));
});
