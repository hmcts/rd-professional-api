CREATE VIEW super_user_view as select * from professional_user
where id in (select professional_user_id from user_attribute where prd_enum_code = 4 AND prd_enum_type like 'ADMIN_ROLE');

CREATE INDEX organisation_identifier_idx ON organisation (organisation_identifier);
CREATE INDEX status_idx ON organisation (status);

CREATE INDEX organisation_id_idx1 ON professional_user (organisation_id);
CREATE INDEX user_identifier_idx ON professional_user (user_identifier);
CREATE INDEX email_address_idx ON professional_user (email_address);

CREATE INDEX organisation_id_idx2 ON payment_account (organisation_id);

CREATE INDEX organisation_id_idx3 ON contact_information (organisation_id);

CREATE INDEX contact_information_id_idx ON dx_address (contact_information_id);

CREATE INDEX professional_user_id_idx1 ON user_attribute (professional_user_id);

CREATE INDEX professional_user_id_idx2 ON user_account_map (professional_user_id);
CREATE INDEX payment_account_id_idx ON user_account_map (payment_account_id);

CREATE INDEX professional_user_id_idx3 ON user_address_map (professional_user_id);
CREATE INDEX contact_address_id_idx ON user_address_map (contact_address_id);

CREATE INDEX organisation_id_idx4 ON domain (organisation_id);