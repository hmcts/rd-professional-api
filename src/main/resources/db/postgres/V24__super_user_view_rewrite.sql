CREATE OR REPLACE VIEW dbrefdata.super_user_view AS
SELECT pu.id,
       pu.first_name,
       pu.last_name,
       pu.email_address,
       pu.organisation_id,
       pu.last_updated,
       pu.created,
       pu.user_identifier,
       pu.deleted
FROM dbrefdata.professional_user pu
JOIN dbrefdata.user_attribute ua
  ON pu.id = ua.professional_user_id
WHERE ua.prd_enum_code = 4
  AND ua.prd_enum_type = 'ADMIN_ROLE';
