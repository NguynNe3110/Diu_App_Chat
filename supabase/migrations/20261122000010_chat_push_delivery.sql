-- Apply after schema migrations 00001-00009. Webhook is configured separately.
BEGIN;
CREATE TABLE IF NOT EXISTS public.chat_push_deliveries (
    message_id uuid NOT NULL REFERENCES public.messages(id) ON DELETE CASCADE,
    device_id uuid NOT NULL REFERENCES public.push_tokens(id) ON DELETE CASCADE,
    status text NOT NULL CHECK (status IN ('processing', 'accepted', 'failed')),
    lease_id uuid NOT NULL DEFAULT gen_random_uuid(),
    lease_until timestamptz NOT NULL,
    attempts integer NOT NULL DEFAULT 1,
    updated_at timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (message_id, device_id)
);
ALTER TABLE public.chat_push_deliveries ENABLE ROW LEVEL SECURITY;
REVOKE ALL ON public.chat_push_deliveries FROM PUBLIC, anon, authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.chat_push_deliveries TO service_role;

-- Re-read canonical rows; webhook names/content/recipient IDs are never trusted.
-- color omits sender/content even from data; name omits content; full caps preview.
CREATE OR REPLACE FUNCTION public.chat_push_targets(p_message_id uuid)
RETURNS TABLE (
    device_id uuid, recipient_id uuid, token text,
    conversation_id uuid, title text, body text, mood text
)
LANGUAGE sql STABLE SECURITY DEFINER SET search_path = public, pg_temp
AS $$
    SELECT pt.id, cm.user_id, pt.token, m.conversation_id,
        CASE WHEN us.notif_display::text IN ('name', 'full')
            THEN coalesce(nullif(up.display_name, ''), 'Người thương')::text
            ELSE 'Dịu' END,
        CASE WHEN us.notif_display::text = 'full' THEN
            CASE m.type::text
                WHEN 'text' THEN left(coalesce(nullif(m.content, ''), 'Bạn có tin nhắn mới'), 160)
                WHEN 'photo' THEN 'Đã gửi một ảnh'
                WHEN 'voice' THEN 'Đã gửi tin nhắn thoại'
                WHEN 'location' THEN 'Đã chia sẻ vị trí'
            END
            ELSE 'Bạn có tin nhắn mới' END,
        m.mood_id::text
    FROM public.messages m
    JOIN public.user_profiles up ON up.id = m.sender_id
    JOIN public.conversations c ON c.id = m.conversation_id
    JOIN public.conversation_members sender ON sender.conversation_id = m.conversation_id
        AND sender.user_id = m.sender_id AND sender.left_at IS NULL
    JOIN public.conversation_members cm ON cm.conversation_id = m.conversation_id
        AND cm.user_id <> m.sender_id AND cm.left_at IS NULL
        AND cm.is_muted IS NOT TRUE AND cm.joined_at <= m.created_at
    JOIN public.push_tokens pt ON pt.user_id = cm.user_id AND pt.platform = 'android'
    LEFT JOIN public.user_settings us ON us.user_id = cm.user_id
    WHERE m.id = p_message_id AND m.deleted_at IS NULL
        AND m.type::text IN ('text', 'photo', 'voice', 'location')
        AND NOT EXISTS (
            SELECT 1 FROM public.blocks b
            WHERE (b.blocker_id = m.sender_id AND b.blocked_id = cm.user_id)
                OR (b.blocker_id = cm.user_id AND b.blocked_id = m.sender_id)
        )
        AND NOT public.is_in_quiet_hours(cm.user_id)
        -- Digest is not implemented here; do not send immediate group pushes to opt-ins.
        AND NOT (c.type::text = 'group' AND coalesce(us.group_digest, true));
$$;

CREATE OR REPLACE FUNCTION public.claim_chat_push(p_message_id uuid, p_device_id uuid)
RETURNS uuid
LANGUAGE plpgsql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
DECLARE v_lease uuid;
BEGIN
    INSERT INTO public.chat_push_deliveries AS d
        (message_id, device_id, status, lease_until)
    VALUES (p_message_id, p_device_id, 'processing', now() + interval '2 minutes')
    ON CONFLICT (message_id, device_id) DO UPDATE
        SET status = 'processing', lease_id = gen_random_uuid(),
            lease_until = now() + interval '2 minutes',
            attempts = d.attempts + 1, updated_at = now()
        WHERE d.attempts < 5 AND (
            d.status = 'failed' OR
            (d.status = 'processing' AND d.lease_until < now())
        )
    RETURNING lease_id INTO v_lease;
    RETURN v_lease;
END;
$$;

CREATE OR REPLACE FUNCTION public.finish_chat_push(
    p_message_id uuid, p_device_id uuid, p_lease_id uuid, p_accepted boolean
) RETURNS void
LANGUAGE sql SECURITY DEFINER SET search_path = public, pg_temp
AS $$
    UPDATE public.chat_push_deliveries
    SET status = CASE WHEN p_accepted THEN 'accepted' ELSE 'failed' END,
        updated_at = now()
    WHERE message_id = p_message_id AND device_id = p_device_id
        AND lease_id = p_lease_id AND status = 'processing';
$$;

REVOKE ALL ON FUNCTION public.chat_push_targets(uuid) FROM PUBLIC, anon, authenticated;
REVOKE ALL ON FUNCTION public.claim_chat_push(uuid, uuid) FROM PUBLIC, anon, authenticated;
REVOKE ALL ON FUNCTION public.finish_chat_push(uuid, uuid, uuid, boolean) FROM PUBLIC, anon, authenticated;
GRANT EXECUTE ON FUNCTION public.chat_push_targets(uuid) TO service_role;
GRANT EXECUTE ON FUNCTION public.claim_chat_push(uuid, uuid) TO service_role;
GRANT EXECUTE ON FUNCTION public.finish_chat_push(uuid, uuid, uuid, boolean) TO service_role;
COMMIT;
