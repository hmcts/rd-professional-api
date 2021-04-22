insert into organisation_mfa_status (organisation_id, created, last_updated, mfa_status)
select id, timezone('utc', now()), timezone('utc', now()), 'EMAIL' from organisation