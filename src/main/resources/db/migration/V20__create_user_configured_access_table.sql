-- dataload_schedular_job definition
create table user_configured_access(
	professional_user_id uuid NOT NULL,
	jurisdiction_id varchar(255) NOT NULL,
	organisation_profile_id  varchar(255) NOT NULL,
	access_type_id  varchar(255) NOT NULL,
	enabled boolean,
	constraint user_configured_access_pk primary key (professional_user_id, jurisdiction_id, organisation_profile_id, access_type_id)
);

alter table user_configured_access add constraint professional_user_fk6 foreign key (professional_user_id)
references professional_user (id) on delete cascade;
