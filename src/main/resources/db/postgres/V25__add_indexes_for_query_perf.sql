CREATE INDEX IF NOT EXISTS idx_organisation_status_last_updated_id
    ON dbrefdata.organisation (status, last_updated, id);

CREATE INDEX IF NOT EXISTS idx_organisation_org_type_id
    ON dbrefdata.organisation (org_type, id);

CREATE INDEX IF NOT EXISTS idx_professional_user_org_user_identifier
    ON dbrefdata.professional_user (organisation_id, user_identifier);

CREATE INDEX IF NOT EXISTS idx_professional_user_last_updated_id
    ON dbrefdata.professional_user (last_updated, id);

CREATE INDEX IF NOT EXISTS idx_user_attribute_prof_user_enum
    ON dbrefdata.user_attribute (professional_user_id, prd_enum_code, prd_enum_type);

CREATE INDEX IF NOT EXISTS idx_user_account_map_payment_prof
    ON dbrefdata.user_account_map (payment_account_id, professional_user_id);
