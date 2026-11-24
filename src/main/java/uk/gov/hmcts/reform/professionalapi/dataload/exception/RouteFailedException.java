package uk.gov.hmcts.reform.professionalapi.dataload.exception;

public class RouteFailedException extends RuntimeException {

    public RouteFailedException(String message) {
        super(message);
    }
}
