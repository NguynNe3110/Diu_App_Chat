\set ON_ERROR_STOP on
-- ONLY run in a disposable, empty PostgreSQL database as superuser.
-- Minimal schema reproduces the existing push_tokens/profile relationship.
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'anon') THEN CREATE ROLE anon NOLOGIN; END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'authenticated') THEN CREATE ROLE authenticated NOLOGIN; END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'service_role') THEN CREATE ROLE service_role NOLOGIN BYPASSRLS; END IF;
END $$;
CREATE SCHEMA auth;
CREATE FUNCTION auth.uid() RETURNS uuid LANGUAGE sql STABLE AS
$$ SELECT nullif(current_setting('request.jwt.claim.sub', true), '')::uuid $$;
GRANT USAGE ON SCHEMA auth TO authenticated, service_role;
GRANT EXECUTE ON FUNCTION auth.uid() TO authenticated, service_role;
CREATE TABLE public.user_profiles (id uuid PRIMARY KEY);
CREATE FUNCTION public.set_updated_at() RETURNS trigger LANGUAGE plpgsql AS
$$ BEGIN NEW.updated_at = now(); RETURN NEW; END $$;
INSERT INTO public.user_profiles VALUES
('00000000-0000-0000-0000-000000000001'),
('00000000-0000-0000-0000-000000000002');

-- Existing schema and legacy data; migration must preserve it.
CREATE TABLE public.push_tokens (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id uuid NOT NULL REFERENCES public.user_profiles(id) ON DELETE CASCADE,
    token text NOT NULL,
    platform varchar(10) NOT NULL CHECK (platform IN ('android', 'ios', 'web')),
    created_at timestamptz DEFAULT now(),
    updated_at timestamptz DEFAULT now(),
    CONSTRAINT uq_push_token UNIQUE (user_id, token)
);
ALTER TABLE public.push_tokens ENABLE ROW LEVEL SECURITY;
CREATE POLICY "Push: quản lý token của mình" ON public.push_tokens FOR ALL
    USING (user_id = auth.uid()) WITH CHECK (user_id = auth.uid());
-- Simulate an accidentally permissive deployment policy and legacy grants.
CREATE POLICY accidental_read ON public.push_tokens FOR SELECT TO authenticated USING (true);
GRANT ALL ON public.push_tokens TO anon, authenticated;
GRANT UPDATE (user_id) ON public.push_tokens TO authenticated;
INSERT INTO public.push_tokens (user_id, token, platform) VALUES
('00000000-0000-0000-0000-000000000002', 'synthetic-legacy-b', 'android');

\ir ../migrations/20261122000009_push_tokens_registration.sql
-- Reapplication must be safe and preserve rows.
\ir ../migrations/20261122000009_push_tokens_registration.sql

BEGIN;
CREATE FUNCTION pg_temp.assert_true(ok boolean, label text) RETURNS void LANGUAGE plpgsql AS
$$ BEGIN
    IF ok IS DISTINCT FROM true THEN RAISE EXCEPTION 'FAIL: %', label; END IF;
    RAISE NOTICE 'PASS: %', label;
END $$;
SELECT pg_temp.assert_true(
    (SELECT count(*) = 1 FROM public.push_tokens WHERE token = 'synthetic-legacy-b'),
    'legacy registration survives migration and reapplication');

SET LOCAL ROLE anon;
DO $$ BEGIN
    BEGIN
        PERFORM * FROM public.push_tokens;
        RAISE EXCEPTION 'anon read unexpectedly allowed';
    EXCEPTION WHEN insufficient_privilege THEN NULL; END;
    BEGIN
        INSERT INTO public.push_tokens (token, platform) VALUES ('synthetic-anon', 'android');
        RAISE EXCEPTION 'anon insert unexpectedly allowed';
    EXCEPTION WHEN insufficient_privilege THEN NULL; END;
END $$;
RESET ROLE;

SET LOCAL ROLE authenticated;
SELECT set_config('request.jwt.claim.sub', '00000000-0000-0000-0000-000000000001', true);
SELECT pg_temp.assert_true((SELECT count(*) = 0 FROM public.push_tokens),
    'A cannot read B even with an extra permissive policy');
INSERT INTO public.push_tokens (token, platform, installation_id)
VALUES ('synthetic-a1', 'android', '10000000-0000-0000-0000-000000000001');
INSERT INTO public.push_tokens (token, platform, installation_id)
VALUES ('synthetic-a2', 'android', '10000000-0000-0000-0000-000000000002');
SELECT pg_temp.assert_true((SELECT count(*) = 2 FROM public.push_tokens),
    'owner default and multiple installations');

DO $$ BEGIN
    BEGIN
        INSERT INTO public.push_tokens (user_id, token, platform)
        VALUES ('00000000-0000-0000-0000-000000000002', 'synthetic-forged', 'android');
        RAISE EXCEPTION 'forged owner unexpectedly allowed';
    EXCEPTION WHEN insufficient_privilege THEN NULL; END;
    BEGIN
        UPDATE public.push_tokens SET user_id = '00000000-0000-0000-0000-000000000002';
        RAISE EXCEPTION 'owner reassignment unexpectedly allowed';
    EXCEPTION WHEN insufficient_privilege THEN NULL; END;
    BEGIN
        INSERT INTO public.push_tokens (token, platform) VALUES ('   ', 'android');
        RAISE EXCEPTION 'blank token unexpectedly allowed';
    EXCEPTION WHEN check_violation THEN NULL; END;
    BEGIN
        INSERT INTO public.push_tokens (token, platform) VALUES ('synthetic-invalid', 'other');
        RAISE EXCEPTION 'invalid platform unexpectedly allowed';
    EXCEPTION WHEN check_violation THEN NULL; END;
    BEGIN
        INSERT INTO public.push_tokens (token, platform, installation_id)
        VALUES ('synthetic-duplicate-install', 'android', '10000000-0000-0000-0000-000000000001');
        RAISE EXCEPTION 'duplicate installation unexpectedly allowed';
    EXCEPTION WHEN unique_violation THEN NULL; END;
    BEGIN
        INSERT INTO public.push_tokens (token, platform) VALUES ('synthetic-legacy-b', 'android');
        RAISE EXCEPTION 'shared endpoint unexpectedly allowed';
    EXCEPTION WHEN unique_violation THEN NULL; END;
    RAISE NOTICE 'PASS: forged owner, immutable owner, validation and uniqueness rejected';
END $$;

UPDATE public.push_tokens SET token = 'synthetic-a1-rotated' WHERE token = 'synthetic-a1';
SELECT pg_temp.assert_true(
    EXISTS (SELECT 1 FROM public.push_tokens WHERE token = 'synthetic-a1-rotated'),
    'owner rotates token');
WITH changed AS (
    UPDATE public.push_tokens SET token = 'synthetic-stolen' WHERE token = 'synthetic-legacy-b' RETURNING id
) SELECT pg_temp.assert_true((SELECT count(*) = 0 FROM changed), 'A cannot update B');
WITH removed AS (
    DELETE FROM public.push_tokens WHERE token = 'synthetic-legacy-b' RETURNING id
) SELECT pg_temp.assert_true((SELECT count(*) = 0 FROM removed), 'A cannot delete B');
WITH removed AS (
    DELETE FROM public.push_tokens WHERE token = 'synthetic-a1-rotated' RETURNING id
) SELECT pg_temp.assert_true((SELECT count(*) = 1 FROM removed), 'owner detaches on logout');

SELECT set_config('request.jwt.claim.sub', '00000000-0000-0000-0000-000000000002', true);
SELECT pg_temp.assert_true((SELECT count(*) = 1 FROM public.push_tokens), 'B sees only B');
SELECT set_config('request.jwt.claim.sub', '', true);
SELECT pg_temp.assert_true((SELECT count(*) = 0 FROM public.push_tokens), 'missing UID sees nothing');
RESET ROLE;

SET LOCAL ROLE service_role;
SELECT pg_temp.assert_true((SELECT count(*) = 2 FROM public.push_tokens),
    'trusted delivery backend reads endpoints across accounts');
RESET ROLE;
DELETE FROM public.user_profiles WHERE id = '00000000-0000-0000-0000-000000000002';
SELECT pg_temp.assert_true(
    NOT EXISTS (SELECT 1 FROM public.push_tokens WHERE token = 'synthetic-legacy-b'),
    'profile deletion cascades');
ROLLBACK;
\echo push_tokens regression checks passed
