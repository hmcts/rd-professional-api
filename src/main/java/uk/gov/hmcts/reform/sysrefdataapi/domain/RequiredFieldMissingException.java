package uk.gov.hmcts.reform.sysrefdataapi.domain;

public class RequiredFieldMissingException extends RuntimeException {
    public RequiredFieldMissingException(String message) {
        super(message);
    }
}
