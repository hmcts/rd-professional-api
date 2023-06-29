--RDCC 6837 PRD- data model changes for other orgs

CREATE TABLE dbrefdata.org_attributes (
            id uuid NOT NULL,
            organisation_id uuid NOT NULL,
            key varchar(256) NOT NULL,
            value varchar(256) NOT NULL,
            CONSTRAINT org_attributes_pk PRIMARY KEY (id),
            CONSTRAINT org_attributes_fk FOREIGN KEY (organisation_id) REFERENCES dbrefdata.organisation(id));