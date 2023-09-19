-- dataload_schedular_job definition

INSERT INTO organisation (id,"name",status,sra_regulated,company_number,last_updated,created,organisation_identifier,company_url,sra_id,status_message,date_approved) VALUES
	 ('046b6c7f-0b8a-43b9-b35d-6489e6daee91','Uday2 Test Org','ACTIVE',false,NULL,'2019-08-16 15:00:41.418','2019-08-16 14:56:47.227','W98ZZ5W',NULL,'SRA1234562134',NULL,NULL);

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

CREATE TABLE dataload_exception_records(
 id SERIAL NOT NULL,
 table_Name varchar(64),
 scheduler_name varchar(64) NOT NULL,
 scheduler_start_time timestamp NOT NULL,
 key varchar(256),
 field_in_error varchar(256),
 error_description varchar(512),
 updated_timestamp timestamp NOT NULL,
 row_id bigint,
 CONSTRAINT dataload_exception_records_pk PRIMARY KEY (ID)
);

CREATE TABLE dataload_schedular_audit(
  id serial NOT NULL,
  scheduler_name varchar(64) NOT NULL,
  file_name varchar(128),
  scheduler_start_time timestamp NOT NULL,
  scheduler_end_time timestamp,
  status varchar(32),
  CONSTRAINT dataload_schedular_audit_pk PRIMARY KEY (id)
);