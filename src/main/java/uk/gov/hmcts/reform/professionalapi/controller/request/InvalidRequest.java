package uk.gov.hmcts.reform.professionalapi.controller.request;

public class InvalidRequest extends RuntimeException {

    public InvalidRequest(String message) {
        super(message);
    }
}
