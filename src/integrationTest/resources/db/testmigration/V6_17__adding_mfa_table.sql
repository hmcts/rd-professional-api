create table organisation_mfa_status(
	organisation_id uuid primary key not null,
	mfa_status varchar(50) default 'EMAIL' not null,
	created timestamp not null,
	last_updated timestamp
);


alter table organisation_mfa_status add constraint organisation_id_fk foreign key (organisation_id) references organisation (id);