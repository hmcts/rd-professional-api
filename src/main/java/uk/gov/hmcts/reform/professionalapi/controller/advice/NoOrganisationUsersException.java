package uk.gov.hmcts.reform.professionalapi.controller.advice;

public class NoOrganisationUsersException extends ResourceNotFoundException {

    public NoOrganisationUsersException(String message) {
        super(message);
    }
}
