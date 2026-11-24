alter table payment_account add COLUMN pba_status VARCHAR(10) default 'PENDING' not null;

alter table payment_account add COLUMN status_message VARCHAR(256);

alter table payment_account
add CONSTRAINT pba_status_values_constraint
CHECK (pba_status IN ('PENDING','ACCEPTED','REJECTED'));