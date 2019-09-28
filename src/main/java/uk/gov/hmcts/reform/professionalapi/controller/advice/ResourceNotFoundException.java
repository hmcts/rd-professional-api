package uk.gov.hmcts.reform.professionalapi.controller.advice;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}