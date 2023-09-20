create table dataload_schedular_audit(
    id serial4 NOT NULL,
    scheduler_name varchar(64) not null,
    file_name varchar(128),
    scheduler_start_time TIMESTAMP NOT NULL,
    scheduler_end_time TIMESTAMP ,
    status VARCHAR(32),
    constraint dataload_schedular_audit_pk primary key (id)
);


create table dataload_exception_records(
    id serial4 NOT NULL,
    table_name varchar(64) NULL,
    scheduler_name varchar(64) NOT NULL,
    scheduler_start_time TIMESTAMP NOT NULL,
    "key" varchar(256) NULL,
    field_in_error varchar(256) NULL,
    error_description varchar(512) NULL,
    updated_timestamp TIMESTAMP NOT NULL,
    row_id int8 NULL,
    constraint dataload_exception_records_pk primary key (id)
);

COMMIT;