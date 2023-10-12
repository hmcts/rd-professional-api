-- dataload_schedular_job definition

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

