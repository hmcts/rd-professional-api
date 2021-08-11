-- This script is separate from V6__19 due to issues with Unit & Integration tests using H2 Syntax which does not allow 'FROM' or 'INNER JOIN'

update payment_account
set pba_status = 'ACCEPTED', last_updated = now()
FROM organisation
WHERE payment_account.organisation_id::text = organisation.organisation_identifier::text
AND organisation.status = 'ACTIVE';