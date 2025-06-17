CREATE INDEX IF NOT EXISTS idx_payment_account_organisation_id ON dbrefdata.payment_account (organisation_id);
CREATE INDEX IF NOT EXISTS idx_contact_information_organisation_id ON dbrefdata.contact_information (organisation_id);
CREATE INDEX IF NOT EXISTS idx_professional_user_org_id_id ON dbrefdata.professional_user (organisation_id, id);
CREATE INDEX IF NOT EXISTS idx_dx_address_contact_information_id ON dbrefdata.dx_address (contact_information_id);
CREATE INDEX IF NOT EXISTS idx_organisation_status ON dbrefdata.organisation (status);
CREATE INDEX IF NOT EXISTS idx_user_attribute_admin_role ON dbrefdata.user_attribute (professional_user_id) WHERE prd_enum_code = 4 AND prd_enum_type = 'ADMIN_ROLE';
