package uk.gov.hmcts.reform.professionalapi.controller.advice;

import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.ACCESS_EXCEPTION_CCD;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.INVALID_REQUEST_CCD;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.MISSING_TOKEN;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.RESOURCE_NOT_FOUND;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.UNKNOWN_EXCEPTION_CCD;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.USER_EXISTS;

import org.springframework.http.HttpStatus;

@SuppressWarnings("HideUtilityClassConstructor")
public class CcdErrorMessageResolver {

    private CcdErrorMessageResolver() {
    }

    public static String resolveStatusAndReturnMessage(HttpStatus httpStatus) {
        switch (httpStatus) {
            case BAD_REQUEST:
                return INVALID_REQUEST_CCD.getErrorMessage();
            case UNAUTHORIZED:
                return MISSING_TOKEN.getErrorMessage();
            case FORBIDDEN:
                return ACCESS_EXCEPTION_CCD.getErrorMessage();
            case NOT_FOUND:
                return RESOURCE_NOT_FOUND.getErrorMessage();
            case CONFLICT:
                return USER_EXISTS.getErrorMessage();
            default:
                return UNKNOWN_EXCEPTION_CCD.getErrorMessage();
        }
    }
}