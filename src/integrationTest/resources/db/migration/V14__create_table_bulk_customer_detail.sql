-- dataload_schedular_job definition

INSERT INTO organisation (id,name,status,sra_regulated,company_number,last_updated,created,organisation_identifier,company_url,sra_id,status_message,date_approved) VALUES
	 ('046b6c7f-0b8a-43b9-b35d-6489e6daee91','Uday2 Test Org','ACTIVE',false,NULL,'2019-08-16 15:00:41.418','2019-08-16 14:56:47.227','W98ZZ5W',NULL,'SRA1234562134',NULL,NULL);

insert into organisation_mfa_status (organisation_id, created, last_updated, mfa_status)
VALUES ('046b6c7f-0b8a-43b9-b35d-6489e6daee91','2019-08-16 15:00:41.418','2019-08-16 15:00:41.419','EMAIL');

create table bulk_customer_details(
    id uuid,
    organisation_id uuid not null,
    bulk_customer_id varchar(64) not null,
    sidam_id varchar(64) not null,
    pba_number varchar(255) not null,
    constraint customer_details_pk primary key (id),
    constraint organisation_fk7 foreign key (organisation_id)
    references organisation (id)
);

