-- Run AFTER migration 10 and Edge Function deployment.
-- In Supabase Vault first create:
-- chat_push_function_url = https://YOUR_PROJECT.supabase.co/functions/v1/chat-message-push
-- chat_push_webhook_secret = same value as Edge secret CHAT_PUSH_WEBHOOK_SECRET
-- No actual secret belongs in this file. Do not also install a Dashboard webhook.
BEGIN;
CREATE EXTENSION IF NOT EXISTS pg_net WITH SCHEMA extensions;
DO $$
DECLARE v_url text; v_secret text;
BEGIN
    SELECT decrypted_secret INTO v_url FROM vault.decrypted_secrets
        WHERE name = 'chat_push_function_url';
    SELECT decrypted_secret INTO v_secret FROM vault.decrypted_secrets
        WHERE name = 'chat_push_webhook_secret';
    IF v_url IS NULL OR v_url !~ '^https://[a-z0-9]+[.]supabase[.]co/functions/v1/chat-message-push$'
        OR length(coalesce(v_secret, '')) < 32 THEN
        RAISE EXCEPTION 'Configure the two chat_push Vault secrets before installing webhook';
    END IF;
END;
$$;
CREATE OR REPLACE FUNCTION public.queue_chat_message_push()
RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path = ''
AS $$
DECLARE v_url text; v_secret text;
BEGIN
    SELECT decrypted_secret INTO v_url FROM vault.decrypted_secrets
        WHERE name = 'chat_push_function_url';
    SELECT decrypted_secret INTO v_secret FROM vault.decrypted_secrets
        WHERE name = 'chat_push_webhook_secret';
    IF v_url IS NULL OR v_secret IS NULL THEN
        -- Do not break message writes after an operator removes configuration.
        RAISE WARNING 'Chat push webhook configuration missing';
        RETURN NEW;
    END IF;
    PERFORM net.http_post(
        url := v_url,
        headers := jsonb_build_object(
            'Content-Type', 'application/json', 'x-chat-push-secret', v_secret
        ),
        body := jsonb_build_object(
            'type', 'INSERT', 'schema', 'public', 'table', 'messages',
            'record', jsonb_build_object('id', NEW.id)
        ),
        timeout_milliseconds := 120000
    );
    RETURN NEW;
END;
$$;
REVOKE ALL ON FUNCTION public.queue_chat_message_push() FROM PUBLIC, anon, authenticated;
DROP TRIGGER IF EXISTS tr_chat_message_push ON public.messages;
CREATE TRIGGER tr_chat_message_push
    AFTER INSERT ON public.messages
    FOR EACH ROW EXECUTE FUNCTION public.queue_chat_message_push();
COMMIT;
