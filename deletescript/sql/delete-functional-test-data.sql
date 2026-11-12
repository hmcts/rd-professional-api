BEGIN;

create temp table tmp_prd_user_ids on
commit drop as
select
    pu.id
from
    dbrefdata.professional_user pu
where
    pu.email_address like '%@prdfunctestuser.com'
  and pu.created <= CURRENT_TIMESTAMP - interval '1 hour';


create temp table tmp_org_ids on
commit drop as
select
    o.id
from
    dbrefdata.organisation o
        join dbrefdata.professional_user pu on
        o.id = pu.organisation_id
where
    pu.email_address like '%@prdfunctestuser.com'
  and pu.created <= CURRENT_TIMESTAMP - interval '1 hour';

CREATE TEMP TABLE tmp_contacts_ids ON COMMIT DROP AS
SELECT c.id
FROM dbrefdata.contact_information c
         JOIN tmp_org_ids t
              ON c.organisation_id = t.id;


-- BEFORE deleting counts
SELECT 'user_attribute' AS table_name, count(*) AS before_delete_count FROM dbrefdata.user_attribute WHERE professional_user_id IN (SELECT id FROM tmp_prd_user_ids)
UNION ALL
SELECT 'user_account_map', count(*) FROM dbrefdata.user_account_map WHERE professional_user_id IN (SELECT id FROM tmp_prd_user_ids)
UNION ALL
SELECT 'professional_user', count(*) FROM dbrefdata.professional_user WHERE id IN (SELECT id FROM tmp_prd_user_ids)
UNION ALL
SELECT 'payment_account', count(*) FROM dbrefdata.payment_account WHERE organisation_id IN (SELECT id FROM tmp_org_ids)
UNION ALL
SELECT 'dx_address', count(*) FROM dbrefdata.dx_address WHERE contact_information_id IN (SELECT id FROM tmp_contacts_ids)
UNION ALL
SELECT 'contact_information', count(*) FROM dbrefdata.contact_information WHERE id IN (SELECT id FROM tmp_contacts_ids)
UNION ALL
SELECT 'organisation_mfa_status', count(*) FROM dbrefdata.organisation_mfa_status WHERE organisation_id IN (SELECT id FROM tmp_org_ids)
UNION ALL
SELECT 'org_attributes', count(*) FROM dbrefdata.org_attributes WHERE organisation_id IN (SELECT id FROM tmp_org_ids)
UNION ALL
SELECT 'organisation', count(*) FROM dbrefdata.organisation WHERE id IN (SELECT id FROM tmp_org_ids);


-- Deletes and prints the actual rows removed per table
WITH del_user_attribute  AS (DELETE FROM dbrefdata.user_attribute  WHERE professional_user_id IN (SELECT id FROM tmp_prd_user_ids) RETURNING 1),
    del_user_account_map  AS (DELETE FROM dbrefdata.user_account_map WHERE professional_user_id IN (SELECT id FROM tmp_prd_user_ids) RETURNING 1),
    del_professional_user  AS (DELETE FROM dbrefdata.professional_user WHERE id IN (SELECT id FROM tmp_prd_user_ids) RETURNING 1),
    del_payment_account AS (DELETE FROM dbrefdata.payment_account WHERE organisation_id IN (SELECT id FROM tmp_org_ids) RETURNING 1),
    del_dx_address AS (DELETE FROM dbrefdata.dx_address WHERE contact_information_id IN (SELECT id FROM tmp_contacts_ids) RETURNING 1),
    del_contact_information AS (DELETE FROM dbrefdata.contact_information WHERE id IN (SELECT id FROM tmp_contacts_ids) RETURNING 1),
    del_organisation_mfa_status AS (DELETE FROM dbrefdata.organisation_mfa_status WHERE organisation_id IN (SELECT id FROM tmp_org_ids) RETURNING 1),
    del_org_attributes AS (DELETE FROM dbrefdata.org_attributes WHERE organisation_id IN (SELECT id FROM tmp_org_ids) RETURNING 1),
    del_organisation AS (DELETE FROM dbrefdata.organisation WHERE id IN (SELECT id FROM tmp_org_ids) RETURNING 1)
SELECT (SELECT count(*) FROM del_user_attribute)      	  AS deleted_user_attribute,
       (SELECT count(*) FROM del_user_account_map)    	  AS deleted_user_account_map,
       (SELECT count(*) FROM del_professional_user)    	  AS deleted_professional_user,
       (SELECT count(*) FROM del_payment_account)     	  AS deleted_payment_account,
       (SELECT count(*) FROM del_dx_address)          	  AS deleted_dx_address,
       (SELECT count(*) FROM del_contact_information) 	  AS deleted_contact_information,
       (SELECT count(*) FROM del_organisation_mfa_status) AS deleted_organisation_mfa_status,
       (SELECT count(*) FROM del_org_attributes)          AS deleted_org_attributes,
       (SELECT count(*) FROM del_organisation)            AS deleted_organisation;


-- AFTER deleting counts
SELECT 'user_attribute' AS table_name, count(*) AS after_delete_count FROM dbrefdata.user_attribute WHERE professional_user_id IN (SELECT id FROM tmp_prd_user_ids)
UNION ALL
SELECT 'user_account_map', count(*) FROM dbrefdata.user_account_map WHERE professional_user_id IN (SELECT id FROM tmp_prd_user_ids)
UNION ALL
SELECT 'professional_user', count(*) FROM dbrefdata.professional_user WHERE id IN (SELECT id FROM tmp_prd_user_ids)
UNION ALL
SELECT 'payment_account', count(*) FROM dbrefdata.payment_account WHERE organisation_id IN (SELECT id FROM tmp_org_ids)
UNION ALL
SELECT 'dx_address', count(*) FROM dbrefdata.dx_address WHERE contact_information_id IN (SELECT id FROM tmp_contacts_ids)
UNION ALL
SELECT 'contact_information', count(*) FROM dbrefdata.contact_information WHERE id IN (SELECT id FROM tmp_contacts_ids)
UNION ALL
SELECT 'organisation_mfa_status', count(*) FROM dbrefdata.organisation_mfa_status WHERE organisation_id IN (SELECT id FROM tmp_org_ids)
UNION ALL
SELECT 'org_attributes', count(*) FROM dbrefdata.org_attributes WHERE organisation_id IN (SELECT id FROM tmp_org_ids)
UNION ALL
SELECT 'organisation', count(*) FROM dbrefdata.organisation WHERE id IN (SELECT id FROM tmp_org_ids);

COMMIT;

