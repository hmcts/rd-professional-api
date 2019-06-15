ALTER TABLE organisation DROP COLUMN company_url;

ALTER TABLE organisation DROP COLUMN sra_id;

ALTER TABLE organisation ADD COLUMN company_url varchar(512);

ALTER TABLE organisation ADD COLUMN sra_id varchar(255);

INSERT INTO PRD_ENUM (ENUM_CODE, ENUM_NAME, ENUM_TYPE, ENUM_DESC) VALUES (4, 'ORGANISATION_ADMIN', 'PRD_ROLE', 'Identifies the first user of an organisation');
