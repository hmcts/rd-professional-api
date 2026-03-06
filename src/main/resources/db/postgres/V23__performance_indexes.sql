CREATE INDEX IF NOT EXISTS idx_professional_user_org_id_id ON dbrefdata.professional_user (organisation_id, id);
CREATE INDEX IF NOT EXISTS idx_user_attribute_admin_role ON dbrefdata.user_attribute (professional_user_id) WHERE prd_enum_code = 4 AND prd_enum_type = 'ADMIN_ROLE';
