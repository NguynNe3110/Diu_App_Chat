\set ON_ERROR_STOP on
-- Disposable empty PostgreSQL database only. Uses synthetic values, no network.
DO $$ BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='anon') THEN CREATE ROLE anon; END IF;
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='authenticated') THEN CREATE ROLE authenticated; END IF;
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname='service_role') THEN CREATE ROLE service_role BYPASSRLS; END IF;
END $$;
CREATE TABLE public.user_profiles(id uuid PRIMARY KEY, display_name text);
CREATE TABLE public.conversations(id uuid PRIMARY KEY, type text);
CREATE TABLE public.messages(id uuid PRIMARY KEY, conversation_id uuid, sender_id uuid,
  type text, content text, mood_id text, deleted_at timestamptz, created_at timestamptz);
CREATE TABLE public.conversation_members(conversation_id uuid, user_id uuid,
  is_muted boolean DEFAULT false, joined_at timestamptz, left_at timestamptz);
CREATE TABLE public.push_tokens(id uuid PRIMARY KEY, user_id uuid, token text, platform text);
CREATE TABLE public.user_settings(user_id uuid, notif_display text, group_digest boolean);
CREATE TABLE public.blocks(blocker_id uuid, blocked_id uuid);
CREATE TABLE public.test_quiet(user_id uuid);
CREATE FUNCTION public.is_in_quiet_hours(p_user_id uuid) RETURNS boolean LANGUAGE sql AS
$$ SELECT EXISTS(SELECT FROM public.test_quiet WHERE user_id=p_user_id) $$;
GRANT USAGE ON SCHEMA public TO service_role, authenticated, anon;
\ir ../migrations/20261122000010_chat_push_delivery.sql
\ir ../migrations/20261122000010_chat_push_delivery.sql

BEGIN;
CREATE FUNCTION pg_temp.check_true(ok boolean, label text) RETURNS void LANGUAGE plpgsql AS
$$ BEGIN IF ok IS DISTINCT FROM true THEN RAISE EXCEPTION 'FAIL: %', label; END IF;
RAISE NOTICE 'PASS: %', label; END $$;
INSERT INTO public.user_profiles VALUES
('00000000-0000-0000-0000-000000000001','Synthetic Sender'),
('00000000-0000-0000-0000-000000000002','Synthetic Recipient');
INSERT INTO public.conversations VALUES ('10000000-0000-0000-0000-000000000001','direct');
INSERT INTO public.messages VALUES (
'20000000-0000-0000-0000-000000000001','10000000-0000-0000-0000-000000000001',
'00000000-0000-0000-0000-000000000001','text','Synthetic private body','thuong',null,now());
INSERT INTO public.conversation_members(conversation_id,user_id,joined_at)
SELECT '10000000-0000-0000-0000-000000000001',id,now()-interval '1 day' FROM public.user_profiles;
INSERT INTO public.push_tokens VALUES
('30000000-0000-0000-0000-000000000001','00000000-0000-0000-0000-000000000001','synthetic-sender','android'),
('30000000-0000-0000-0000-000000000002','00000000-0000-0000-0000-000000000002','synthetic-b1','android'),
('30000000-0000-0000-0000-000000000003','00000000-0000-0000-0000-000000000002','synthetic-b2','android'),
('30000000-0000-0000-0000-000000000004','00000000-0000-0000-0000-000000000002','synthetic-ios','ios');
CREATE TEMP VIEW targets AS SELECT * FROM public.chat_push_targets('20000000-0000-0000-0000-000000000001');
SELECT pg_temp.check_true((SELECT count(*)=2 FROM targets),'two Android devices; sender and iOS excluded');
SELECT pg_temp.check_true((SELECT bool_and(title='Dịu' AND body='Bạn có tin nhắn mới') FROM targets),'missing settings defaults to private color mode');
INSERT INTO public.user_settings VALUES ('00000000-0000-0000-0000-000000000002','name',false);
SELECT pg_temp.check_true((SELECT bool_and(title='Synthetic Sender' AND body='Bạn có tin nhắn mới') FROM targets),'name mode never contains content');
UPDATE public.user_settings SET notif_display='full';
SELECT pg_temp.check_true((SELECT bool_and(body='Synthetic private body') FROM targets),'full mode reads canonical content');
UPDATE public.messages SET content=repeat('x',300);
SELECT pg_temp.check_true((SELECT bool_and(length(body)=160) FROM targets),'full preview capped');
UPDATE public.messages SET type='photo',content='sensitive-media-url';
SELECT pg_temp.check_true((SELECT bool_and(body='Đã gửi một ảnh') FROM targets),'photo preview excludes media content');
UPDATE public.messages SET type='sealed_letter';
SELECT pg_temp.check_true((SELECT count(*)=0 FROM targets),'sealed letters excluded until separate scheduler');
UPDATE public.messages SET type='system';
SELECT pg_temp.check_true((SELECT count(*)=0 FROM targets),'system events excluded');
UPDATE public.messages SET type='text',deleted_at=now();
SELECT pg_temp.check_true((SELECT count(*)=0 FROM targets),'deleted messages excluded');
UPDATE public.messages SET deleted_at=null;
UPDATE public.conversation_members SET is_muted=true WHERE user_id='00000000-0000-0000-0000-000000000002';
SELECT pg_temp.check_true((SELECT count(*)=0 FROM targets),'muted recipient excluded');
UPDATE public.conversation_members SET is_muted=false,left_at=now() WHERE user_id='00000000-0000-0000-0000-000000000002';
SELECT pg_temp.check_true((SELECT count(*)=0 FROM targets),'departed recipient excluded');
UPDATE public.conversation_members SET left_at=null,joined_at=now()+interval '1 day' WHERE user_id='00000000-0000-0000-0000-000000000002';
SELECT pg_temp.check_true((SELECT count(*)=0 FROM targets),'later join cannot receive historical message');
UPDATE public.conversation_members SET joined_at=now()-interval '1 day';
INSERT INTO public.blocks VALUES ('00000000-0000-0000-0000-000000000001','00000000-0000-0000-0000-000000000002');
SELECT pg_temp.check_true((SELECT count(*)=0 FROM targets),'sender blocking recipient suppresses push');
UPDATE public.blocks SET blocker_id='00000000-0000-0000-0000-000000000002',blocked_id='00000000-0000-0000-0000-000000000001';
SELECT pg_temp.check_true((SELECT count(*)=0 FROM targets),'recipient blocking sender suppresses push');
DELETE FROM public.blocks;
INSERT INTO public.test_quiet VALUES ('00000000-0000-0000-0000-000000000002');
SELECT pg_temp.check_true((SELECT count(*)=0 FROM targets),'quiet helper suppresses push');
DELETE FROM public.test_quiet;
UPDATE public.conversations SET type='group';
UPDATE public.user_settings SET group_digest=true;
SELECT pg_temp.check_true((SELECT count(*)=0 FROM targets),'digest opt-in suppresses immediate group push');
UPDATE public.user_settings SET group_digest=false;
SELECT pg_temp.check_true((SELECT count(*)=2 FROM targets),'group immediate opt-in works');
UPDATE public.conversation_members SET left_at=now() WHERE user_id='00000000-0000-0000-0000-000000000001';
SELECT pg_temp.check_true((SELECT count(*)=0 FROM targets),'sender no longer member suppresses replay');
UPDATE public.conversation_members SET left_at=null;

SET LOCAL ROLE authenticated;
DO $$ BEGIN
  BEGIN PERFORM * FROM public.chat_push_targets('20000000-0000-0000-0000-000000000001');
    RAISE EXCEPTION 'client can execute projection';
  EXCEPTION WHEN insufficient_privilege THEN NULL; END;
  BEGIN PERFORM * FROM public.chat_push_deliveries;
    RAISE EXCEPTION 'client can read receipts';
  EXCEPTION WHEN insufficient_privilege THEN NULL; END;
END $$;
RESET ROLE;
SET LOCAL ROLE service_role;
SELECT pg_temp.check_true((SELECT count(*)=2 FROM public.chat_push_targets('20000000-0000-0000-0000-000000000001')),'service role can execute projection');
RESET ROLE;

DO $$
DECLARE first_lease uuid; second_lease uuid; msg uuid:='20000000-0000-0000-0000-000000000001';
device uuid:='30000000-0000-0000-0000-000000000002';
BEGIN
  first_lease:=public.claim_chat_push(msg,device);
  PERFORM pg_temp.check_true(first_lease IS NOT NULL,'first claim succeeds');
  PERFORM pg_temp.check_true(public.claim_chat_push(msg,device) IS NULL,'active lease suppresses duplicate');
  PERFORM public.finish_chat_push(msg,device,gen_random_uuid(),true);
  PERFORM pg_temp.check_true((SELECT status='processing' FROM public.chat_push_deliveries WHERE device_id=device),'stale lease cannot finish');
  UPDATE public.chat_push_deliveries SET lease_until=now()-interval '1 second' WHERE device_id=device;
  second_lease:=public.claim_chat_push(msg,device);
  PERFORM pg_temp.check_true(second_lease IS NOT NULL AND second_lease<>first_lease,'expired lease can be reclaimed');
  PERFORM public.finish_chat_push(msg,device,second_lease,false);
  second_lease:=public.claim_chat_push(msg,device);
  PERFORM pg_temp.check_true(second_lease IS NOT NULL,'failed delivery can be retried');
  PERFORM public.finish_chat_push(msg,device,second_lease,true);
  PERFORM pg_temp.check_true(public.claim_chat_push(msg,device) IS NULL,'accepted delivery cannot be reclaimed');
END $$;
ROLLBACK;
\echo Chat push SQL policy checks passed
