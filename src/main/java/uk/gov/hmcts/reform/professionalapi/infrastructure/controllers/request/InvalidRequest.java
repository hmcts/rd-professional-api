package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request;

public class InvalidRequest extends RuntimeException {

    public InvalidRequest(String message) {
        super(message);
    }
}
