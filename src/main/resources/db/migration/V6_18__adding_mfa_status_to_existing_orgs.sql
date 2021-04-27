insert into organisation_mfa_status (organisation_id, created, last_updated, mfa_status)
select id, now(), now(), 'EMAIL' from organisation