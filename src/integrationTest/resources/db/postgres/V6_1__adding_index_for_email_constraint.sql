--CREATE UNIQUE INDEX deleted_not_null_idx ON professional_user (EMAIL_ADDRESS, DELETED)
--HERE DELETED IS NOT NULL;

--CREATE UNIQUE INDEX deleted_null_idx ON professional_user (EMAIL_ADDRESS)
--WHERE DELETED IS NULL;