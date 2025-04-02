CREATE INDEX IF NOT EXISTS idx_bulk_customer_details_bulkCustomerId_sidamId ON dbrefdata.bulk_customer_details (bulk_customer_id, sidam_id);

CREATE INDEX IF NOT EXISTS idx_organisation_status_last_updated ON dbrefdata.organisation (status, last_updated);
CREATE INDEX IF NOT EXISTS idx_organisation_org_type_id ON dbrefdata.organisation (org_type, id);

CREATE INDEX IF NOT EXISTS idx_payment_account_pba_number_organisation_id ON dbrefdata.payment_account (pba_number, organisation_id);
CREATE INDEX IF NOT EXISTS idx_payment_account_pba_number_upper ON dbrefdata.payment_account (upper(pba_number));

CREATE INDEX IF NOT EXISTS idx_professional_user_org_id_id ON dbrefdata.professional_user (organisation_id, id);
CREATE INDEX IF NOT EXISTS idx_professional_user_last_updated_id ON dbrefdata.professional_user (last_updated, id);
