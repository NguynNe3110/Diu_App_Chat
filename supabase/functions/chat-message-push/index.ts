import { createClient } from "npm:@supabase/supabase-js@2";
import { JWT } from "npm:google-auth-library@10";
import { createHandler, type Target } from "./handler.ts";

function required(name: string): string {
  const value = Deno.env.get(name);
  if (!value) throw new Error("Missing server configuration: " + name);
  return value;
}
const supabase = createClient(required("SUPABASE_URL"), required("SUPABASE_SERVICE_ROLE_KEY"), {
  auth: { persistSession: false, autoRefreshToken: false },
});
const account = JSON.parse(required("FIREBASE_SERVICE_ACCOUNT"));
const auth = new JWT({
  email: account.client_email,
  key: account.private_key,
  scopes: ["https://www.googleapis.com/auth/firebase.messaging"],
});

async function targets(messageId: string): Promise<Target[]> {
  const { data, error } = await supabase.rpc("chat_push_targets", { p_message_id: messageId });
  if (error) throw new Error("Target lookup failed");
  return data ?? [];
}
const handler = createHandler(required("CHAT_PUSH_WEBHOOK_SECRET"), {
  targets,
  async claim(messageId, deviceId) {
    const { data, error } = await supabase.rpc("claim_chat_push", {
      p_message_id: messageId, p_device_id: deviceId,
    });
    if (error) throw new Error("Claim failed");
    return data;
  },
  async finish(messageId, deviceId, lease, accepted) {
    const { error } = await supabase.rpc("finish_chat_push", {
      p_message_id: messageId, p_device_id: deviceId,
      p_lease_id: lease, p_accepted: accepted,
    });
    if (error) throw new Error("Receipt failed");
  },
  async removeExpired(target) {
    const { error } = await supabase.from("push_tokens").delete()
      .eq("id", target.device_id).eq("token", target.token);
    if (error) throw new Error("Expired endpoint cleanup failed");
  },
  async send(messageId, target) {
    const credentials = await auth.getAccessToken();
    if (!credentials.token) throw new Error("FCM authorization failed");
    const response = await fetch(
      "https://fcm.googleapis.com/v1/projects/" +
        encodeURIComponent(account.project_id) + "/messages:send",
      {
        method: "POST",
        headers: {
          Authorization: "Bearer " + credentials.token,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          message: {
            token: target.token,
            notification: { title: target.title, body: target.body },
            data: {
              message_id: messageId,
              conversation_id: target.conversation_id,
              mood: target.mood,
            },
            android: {
              priority: "high",
              ttl: "3600s",
              notification: { channel_id: "chat_messages", tag: messageId },
            },
          },
        }),
        signal: AbortSignal.timeout(15_000),
      },
    );
    if (response.ok) return "accepted";
    const result = await response.json();
    const details: { errorCode?: string; "@type"?: string }[] = result.error?.details ?? [];
    if (details.some((detail) =>
      detail["@type"] === "type.googleapis.com/google.firebase.fcm.v1.FcmError" &&
      detail.errorCode === "UNREGISTERED"
    )) return "unregistered";
    // INVALID_ARGUMENT may be a bad payload, not an expired device token.
    throw new Error("FCM delivery failed");
  },
});
Deno.serve(handler);
