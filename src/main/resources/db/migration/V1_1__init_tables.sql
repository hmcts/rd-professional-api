-- NB Flyway requires lowercase for table names
create schema if not exists dbrefdata;

create table organisation(
	id uuid,
	name varchar(255),
	status varchar(50),
	sra_id varchar(255),
	sra_regulated boolean,
	company_number varchar(8),
	company_url varchar(512),
	organisation_identifier uuid,
	last_updated timestamp not null,
	created timestamp not null,
	constraint organisation_pk primary key (id),
	constraint organisation_identifier_uq1 unique (organisation_identifier),
	constraint sra_id_uq1 unique (sra_id),
	constraint company_number_uq1 unique (company_number),
	constraint company_url_uq1 unique (company_url)
);

create table professional_user(
	id uuid not null,
	first_name varchar(255) not null,
	last_name varchar(255) not null,
	email_address varchar(255) not null,
	status varchar(50) not null,
	organisation_id uuid not null,
	last_updated timestamp not null,
	created timestamp not null,
	constraint professional_user_pk primary key (id),
	constraint email_address_uq1 unique (email_address)
);

create table payment_account(
	id uuid not null,
	pba_number varchar(255) not null,
	organisation_id uuid not null,
	last_updated timestamp not null,
	created timestamp not null,
	constraint pba_number_uq unique (pba_number),
	constraint payment_account_pk primary key (id)
);

create table contact_information(
	id uuid not null,
	address_line1 varchar(150) not null,
	address_line2 varchar(50),
	address_line3 varchar(50),
	town_city varchar(50),
	country varchar(50),
	county varchar(50),
	postcode varchar(14),
	organisation_id uuid not null,
	last_updated timestamp not null,
	created timestamp not null,
	constraint contact_information_pk primary key (id)
);

create table dx_address(
	id uuid,
	dx_exchange varchar(20) not null,
	dx_number varchar(13) not null,
	contact_information_id uuid not null,
	last_updated timestamp not null,
	created timestamp not null,
	constraint dx_address_pk primary key (id)
);

create table user_attribute(
	id uuid,
	professional_user_id uuid,
	prd_enum_code smallint,
	prd_enum_type varchar(50),
	constraint professional_user_pk1 primary key (id)
);

create table prd_enum(
	enum_code smallint not null,
	enum_name varchar(50) not null,
	enum_type varchar(50) not null,
	enum_desc varchar(1024),
	constraint prd_enum_uq1 primary key (enum_code,enum_type)
);

create table user_account_map(
	professional_user_id uuid,
	payment_account_id uuid,
	defaulted boolean not null default false,
	constraint pba_mapping_pk primary key (professional_user_id,payment_account_id)
);

create table user_address_map(
	professional_user_id uuid,
	contact_address_id uuid,
	defaulted boolean not null,
	constraint address_mapping_pk primary key (professional_user_id,contact_address_id)
);

create table domain(
	id uuid,
	organisation_id uuid not null,
	domain_identifier uuid not null,
	domain_name varchar(50) not null,
	last_updated timestamp not null,
	created timestamp not null,
	constraint domain_pk primary key (id),
	constraint domain_identifier_uq1 unique (domain_identifier)
);

alter table professional_user add constraint organisation_fk1 foreign key (organisation_id)
references organisation (id);

alter table payment_account add constraint organisation_fk2 foreign key (organisation_id)
references organisation (id);

alter table contact_information add constraint organisation_fk3 foreign key (organisation_id)
references organisation (id);

alter table dx_address add constraint organisation_fk4 foreign key (contact_information_id)
references contact_information (id);

alter table user_attribute add constraint professional_user_fk1 foreign key (professional_user_id)
references professional_user (id);

alter table user_attribute add constraint prd_enum_fk1 foreign key (prd_enum_code,prd_enum_type)
references prd_enum (enum_code,enum_type) ;

alter table user_account_map add constraint pba_fk1 foreign key (payment_account_id)
references payment_account (id);

alter table user_account_map add constraint professional_user_fk4 foreign key (professional_user_id)
references professional_user (id);

alter table user_address_map add constraint professional_user_fk5 foreign key (professional_user_id)
references professional_user (id);

alter table user_address_map add constraint contact_address_fk1 foreign key (contact_address_id)
references contact_information (id);

alter table domain add constraint organisation_fk6 foreign key (organisation_id)
references organisation (id);