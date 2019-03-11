package uk.gov.hmcts.reform.professionalapi.domain;

public class RequiredFieldMissingException extends RuntimeException {
    public RequiredFieldMissingException(String message) {
        super(message);
    }
}
