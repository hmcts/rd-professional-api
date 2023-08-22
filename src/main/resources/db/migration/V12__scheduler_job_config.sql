-- dataload_schedular_job definition
CREATE TABLE dataload_schedular_job(
    id serial  NOT NULL,
    publishing_status VARCHAR(16),
    job_start_time TIMESTAMP NOT NULL,
    job_end_time TIMESTAMP
);