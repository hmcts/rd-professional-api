-- dataload_schedular_job definition

alter table bulk_customer_details drop constraint organisation_fk7;

ALTER TABLE bulk_customer_details DROP COLUMN organisation_id;

ALTER TABLE bulk_customer_details ADD COLUMN organisation_id VARCHAR(7);

ALTER TABLE bulk_customer_details ADD CONSTRAINT organisation_fk7 foreign key (organisation_id)
references organisation (organisation_identifier);
