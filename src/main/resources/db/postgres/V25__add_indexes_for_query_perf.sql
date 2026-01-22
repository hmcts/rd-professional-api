CREATE INDEX IF NOT EXISTS idx_professional_user_org_user_identifier
    ON dbrefdata.professional_user (organisation_id, user_identifier);

CREATE INDEX IF NOT EXISTS idx_professional_user_last_updated_id
    ON dbrefdata.professional_user (last_updated, id);
