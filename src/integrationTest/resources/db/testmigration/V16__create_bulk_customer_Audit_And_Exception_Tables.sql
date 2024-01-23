--Created table for lock details
CREATE TABLE dataload_schedular_audit(
  id serial NOT NULL,
  scheduler_name varchar(64) NOT NULL,
  file_name varchar(128),
  scheduler_start_time timestamp NOT NULL,
  scheduler_end_time timestamp,
  status varchar(32),
  CONSTRAINT dataload_schedular_audit_pk PRIMARY KEY (id)
);

CREATE TABLE dataload_exception_records(
 id SERIAL NOT NULL,
 table_name varchar(64),
 scheduler_name varchar(64) NOT NULL,
 scheduler_start_time timestamp NOT NULL,
 key varchar(256),
 field_in_error varchar(256),
 error_description varchar(512),
 updated_timestamp timestamp NOT NULL,
 row_id bigint,
 CONSTRAINT dataload_exception_records_pk PRIMARY KEY (ID)
);