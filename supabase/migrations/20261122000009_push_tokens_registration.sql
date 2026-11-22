-- Dịu: extend the existing push_tokens table from docs/migrations/00001.
-- Apply after the existing profile migrations. No data is deleted.
BEGIN;

CREATE TABLE IF NOT EXISTS public.push_tokens (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES public.user_profiles(id) ON DELETE CASCADE,
    token text NOT NULL,
    platform varchar(10) NOT NULL CHECK (platform IN ('android', 'ios', 'web')),
    created_at timestamptz DEFAULT now(),
    updated_at timestamptz DEFAULT now(),
    CONSTRAINT uq_push_token UNIQUE (user_id, token)
);

-- NULL preserves legacy registrations. New Android registrations supply a random,
-- persisted UUID per app installation (not a hardware ID).
ALTER TABLE public.push_tokens ADD COLUMN IF NOT EXISTS installation_id uuid;
ALTER TABLE public.push_tokens ALTER COLUMN user_id SET DEFAULT auth.uid();

-- Fail atomically if legacy data contains blank or duplicate endpoints.
-- Investigate ownership before retrying; never silently delete or reassign tokens.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM public.push_tokens WHERE btrim(token) = '') THEN
        RAISE EXCEPTION 'push_tokens contains blank tokens; reconcile before migration';
    END IF;
    IF EXISTS (
        SELECT 1 FROM public.push_tokens GROUP BY platform, token HAVING count(*) > 1
    ) THEN
        RAISE EXCEPTION 'push_tokens contains endpoints shared by accounts; reconcile before migration';
    END IF;
END;
$$;

DO $$
BEGIN
    ALTER TABLE public.push_tokens ADD CONSTRAINT push_tokens_token_not_blank
        CHECK (btrim(token) <> '');
EXCEPTION WHEN duplicate_object THEN NULL;
END;
$$;

CREATE UNIQUE INDEX IF NOT EXISTS push_tokens_endpoint_unique
    ON public.push_tokens (platform, token);
CREATE UNIQUE INDEX IF NOT EXISTS push_tokens_installation_unique
    ON public.push_tokens (user_id, installation_id)
    WHERE installation_id IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_push_user ON public.push_tokens (user_id);

-- Reuse the timestamp helper already defined in the initial schema.
DO $$
BEGIN
    CREATE TRIGGER tr_push_tokens_updated
        BEFORE UPDATE ON public.push_tokens
        FOR EACH ROW EXECUTE FUNCTION public.set_updated_at();
EXCEPTION WHEN duplicate_object THEN NULL;
END;
$$;

ALTER TABLE public.push_tokens ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Push: quản lý token của mình" ON public.push_tokens;
DROP POLICY IF EXISTS push_tokens_owner ON public.push_tokens;
CREATE POLICY push_tokens_owner ON public.push_tokens
    FOR ALL TO authenticated
    USING (user_id = (SELECT auth.uid()))
    WITH CHECK (user_id = (SELECT auth.uid()));

-- Defense against any additional permissive policy on an existing deployment.
DROP POLICY IF EXISTS push_tokens_owner_guard ON public.push_tokens;
CREATE POLICY push_tokens_owner_guard ON public.push_tokens
    AS RESTRICTIVE FOR ALL TO authenticated
    USING (user_id = (SELECT auth.uid()))
    WITH CHECK (user_id = (SELECT auth.uid()));

REVOKE ALL ON public.push_tokens FROM PUBLIC, anon, authenticated;
-- Remove legacy column grants as well as table-level grants.
REVOKE ALL (id, user_id, token, platform, installation_id, created_at, updated_at)
    ON public.push_tokens FROM PUBLIC, anon, authenticated;
GRANT USAGE ON SCHEMA public TO authenticated, service_role;
GRANT SELECT, DELETE ON public.push_tokens TO authenticated;
GRANT INSERT (user_id, token, platform, installation_id)
    ON public.push_tokens TO authenticated;
GRANT UPDATE (token, platform, installation_id)
    ON public.push_tokens TO authenticated;
GRANT SELECT, INSERT, UPDATE, DELETE ON public.push_tokens TO service_role;

COMMENT ON TABLE public.push_tokens IS
    'Private push endpoints. Owner CRUD; trusted backend delivery. Detach before logout.';
COMMENT ON COLUMN public.push_tokens.installation_id IS
    'Random app installation UUID; NULL for legacy rows. Not an Android hardware ID.';

COMMIT;
