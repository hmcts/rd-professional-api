CREATE TABLE dbrefdata.active_search_request (
    request_id UUID NOT NULL,
    caller_digest VARCHAR(64) NOT NULL,
    cancelled BOOLEAN NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP + INTERVAL '1 minute',
    PRIMARY KEY (request_id, caller_digest)
);

CREATE INDEX active_search_request_expiry ON dbrefdata.active_search_request (expires_at);
