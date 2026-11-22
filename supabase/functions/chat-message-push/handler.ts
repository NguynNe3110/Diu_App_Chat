export type Target = {
  device_id: string;
  recipient_id: string;
  token: string;
  conversation_id: string;
  title: string;
  body: string;
  mood: string;
};
export interface Ports {
  targets(messageId: string): Promise<Target[]>;
  claim(messageId: string, deviceId: string): Promise<string | null>;
  finish(messageId: string, deviceId: string, lease: string, accepted: boolean): Promise<void>;
  send(messageId: string, target: Target): Promise<"accepted" | "unregistered">;
  removeExpired(target: Target): Promise<void>;
}
const uuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export function createHandler(secret: string, ports: Ports) {
  if (secret.length < 32) throw new Error("CHAT_PUSH_WEBHOOK_SECRET must have at least 32 characters");
  return async (req: Request): Promise<Response> => {
    if (req.method !== "POST") return new Response(null, { status: 405 });
    if (req.headers.get("x-chat-push-secret") !== secret) {
      return new Response(null, { status: 401 });
    }
    let messageId: string;
    try {
      const payload = await req.json();
      if (payload?.type !== "INSERT" || payload?.schema !== "public" ||
        payload?.table !== "messages" || typeof payload?.record?.id !== "string" ||
        !uuid.test(payload.record.id)) {
        return Response.json({ error: "Expected public.messages INSERT webhook" }, { status: 400 });
      }
      messageId = payload.record.id;
    } catch {
      return Response.json({ error: "Invalid JSON" }, { status: 400 });
    }
    try {
      const targets = await ports.targets(messageId);
      let accepted = 0, skipped = 0, failed = 0;
      // Small groups; sequential sends avoid an unbounded fan-out.
      for (const initial of targets) {
        const lease = await ports.claim(messageId, initial.device_id);
        if (!lease) { skipped++; continue; }
        let handedToFcm = false;
        try {
          // Re-read current mute/block/privacy and current token after claiming.
          const current = (await ports.targets(messageId))
            .find((item) => item.device_id === initial.device_id);
          if (!current) {
            await ports.finish(messageId, initial.device_id, lease, true);
            skipped++;
            continue;
          }
          const outcome = await ports.send(messageId, current);
          handedToFcm = outcome === "accepted";
          if (outcome === "unregistered") await ports.removeExpired(current);
          await ports.finish(messageId, current.device_id, lease, true);
          if (handedToFcm) accepted++; else skipped++;
        } catch {
          // If FCM accepted but persistence failed, preserve the lease; do not
          // immediately retry and spam. A later replay can still duplicate it.
          if (!handedToFcm) {
            await ports.finish(messageId, initial.device_id, lease, false);
          }
          failed++;
        }
      }
      return Response.json({ accepted, skipped, failed }, { status: failed ? 503 : 200 });
    } catch {
      // Never log request bodies, recipient IDs, tokens or SDK response bodies.
      return Response.json({ error: "Chat push processing failed" }, { status: 503 });
    }
  };
}
