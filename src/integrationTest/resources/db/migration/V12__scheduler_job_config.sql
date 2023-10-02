-- dataload_schedular_job definition
CREATE TABLE dataload_schedular_job(
    id serial  NOT NULL,
    publishing_status VARCHAR(16),
    job_start_time TIMESTAMP NOT NULL,
    job_end_time TIMESTAMP
);

CREATE TABLE lock_details_provider(name VARCHAR(64) NOT NULL, lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL, locked_by VARCHAR(255) NOT NULL, PRIMARY KEY (name));