CREATE TABLE audit (
            id uuid NOT NULL,
            organisation_id varchar(50) NOT NULL,
            change_details varchar(256) NOT NULL,
            updated_by varchar(256) NOT NULL,
            change_action varchar(256) NOT NULL,
            created timestamp not null,
            last_updated timestamp not null,
            CONSTRAINT audit_pk PRIMARY KEY (id)
            );

commit;