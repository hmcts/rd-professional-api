ALTER TABLE PRD_ENUM ADD COLUMN ENABLED VARCHAR(8) DEFAULT 'NO' NOT NULL;

UPDATE PRD_ENUM SET ENABLED = 'YES';

INSERT INTO PRD_ENUM (ENUM_CODE, ENUM_NAME, ENUM_TYPE, ENUM_DESC) VALUES (31, 'caseworker-publiclaw', 'CCD_ROLE', 'caseworker-publiclaw');
INSERT INTO PRD_ENUM (ENUM_CODE, ENUM_NAME, ENUM_TYPE, ENUM_DESC) VALUES (32, 'caseworker-publiclaw-solicitor', 'CCD_ROLE', 'caseworker-publiclaw-solicitor');
INSERT INTO PRD_ENUM (ENUM_CODE, ENUM_NAME, ENUM_TYPE, ENUM_DESC) VALUES (33, 'caseworker-ia-legalrep-solicitor', 'CCD_ROLE', 'caseworker-ia-legalrep-solicitor');
INSERT INTO PRD_ENUM (ENUM_CODE, ENUM_NAME, ENUM_TYPE, ENUM_DESC) VALUES (34, 'PUBLICLAW', 'JURISD_ID', 'Content for the Test Jurisdiction');
INSERT INTO PRD_ENUM (ENUM_CODE, ENUM_NAME, ENUM_TYPE, ENUM_DESC) VALUES (35, 'caseworker-ia', 'CCD_ROLE', 'caseworker-ia');



