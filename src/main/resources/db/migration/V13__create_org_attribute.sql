CREATE TABLE org_attributes (
            id uuid NOT NULL,
            organisation_id uuid NOT NULL,
            key varchar(256) NOT NULL,
            value varchar(256) NOT NULL,
            CONSTRAINT org_attributes_pk PRIMARY KEY (id),
            CONSTRAINT org_attributes_fk FOREIGN KEY (organisation_id) REFERENCES organisation(id));