CREATE TABLE singleton_org_type (
            id serial4 NOT NULL,
            org_type varchar(256) NOT NULL,
            CONSTRAINT singleton_org_type_pk PRIMARY KEY (id),
            CONSTRAINT org_type_uk UNIQUE (org_type));