DROP VIEW super_user_view;

ALTER TABLE professional_user DROP CONSTRAINT organisation_fk1;
alter table professional_user add constraint organisation_fk1 foreign key (organisation_id)
references organisation (id) on delete cascade;

ALTER TABLE payment_account DROP CONSTRAINT organisation_fk2;
alter table payment_account add constraint organisation_fk2 foreign key (organisation_id)
references organisation (id) on delete cascade;

ALTER TABLE contact_information DROP CONSTRAINT organisation_fk3;
alter table contact_information add constraint organisation_fk3 foreign key (organisation_id)
references organisation (id) on delete cascade;

ALTER TABLE dx_address DROP CONSTRAINT organisation_fk4;
alter table dx_address add constraint organisation_fk4 foreign key (contact_information_id)
references contact_information (id) on delete cascade;

ALTER TABLE user_attribute DROP CONSTRAINT professional_user_fk1;
alter table user_attribute add constraint professional_user_fk1 foreign key (professional_user_id)
references professional_user (id) on delete cascade;

ALTER TABLE user_attribute DROP CONSTRAINT professional_user_fk1;
alter table user_attribute add constraint professional_user_fk1 foreign key (professional_user_id)
references professional_user (id) on delete cascade;


ALTER TABLE user_attribute DROP CONSTRAINT prd_enum_fk1;
alter table user_attribute add constraint prd_enum_fk1 foreign key (prd_enum_code,prd_enum_type)
references prd_enum (enum_code,enum_type) on delete cascade;

ALTER TABLE user_account_map DROP CONSTRAINT pba_fk1;
alter table user_account_map add constraint pba_fk1 foreign key (payment_account_id)
references payment_account (id) on delete cascade;

ALTER TABLE user_account_map DROP CONSTRAINT professional_user_fk4;
alter table user_account_map add constraint professional_user_fk4 foreign key (professional_user_id)
references professional_user (id) on delete cascade;

ALTER TABLE user_address_map DROP CONSTRAINT professional_user_fk5;
alter table user_address_map add constraint professional_user_fk5 foreign key (professional_user_id)
references professional_user (id) on delete cascade;

ALTER TABLE user_address_map DROP CONSTRAINT contact_address_fk1;
alter table user_address_map add constraint contact_address_fk1 foreign key (contact_address_id)
references contact_information (id) on delete cascade;

ALTER TABLE domain DROP CONSTRAINT organisation_fk6;
alter table domain add constraint organisation_fk6 foreign key (organisation_id)
references organisation (id) on delete cascade;


create view super_user_view as select * from professional_user where id in (select professional_user_id from user_attribute where prd_enum_code = 4 AND prd_enum_type like 'ADMIN_ROLE');
