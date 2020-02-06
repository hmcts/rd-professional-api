package uk.gov.hmcts.reform.professionalapi.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants;

public class ErrorConstantsTest {

    @Test
    public void check_constants() {

        assertThat(ErrorConstants.MALFORMED_JSON.getErrorMessage()).isEqualTo("1 : Malformed Input Request");

        assertThat(ErrorConstants.UNSUPPORTED_MEDIA_TYPES.getErrorMessage()).isEqualTo("2 : Unsupported Media Type");

        assertThat(ErrorConstants.INVALID_REQUEST.getErrorMessage()).isEqualTo("3 : There is a problem with your request. Please check and try again");

        assertThat(ErrorConstants.EMPTY_RESULT_DATA_ACCESS.getErrorMessage()).isEqualTo("4 : Resource not found");

        assertThat(ErrorConstants.METHOD_ARG_NOT_VALID.getErrorMessage()).isEqualTo("5 : validation on an argument failed");

        assertThat(ErrorConstants.DATA_INTEGRITY_VIOLATION.getErrorMessage()).isEqualTo("6 : %s Invalid or already exists");

        assertThat(ErrorConstants.ILLEGAL_ARGUMENT.getErrorMessage()).isEqualTo("7 : method has been passed an illegal or inappropriate argument");

        assertThat(ErrorConstants.UNKNOWN_EXCEPTION.getErrorMessage()).isEqualTo("8 : error was caused by an unknown exception");

        assertThat(ErrorConstants.ACCESS_EXCEPTION.getErrorMessage()).isEqualTo("9 : Access Denied");

        assertThat(ErrorConstants.CONFLICT_EXCEPTION.getErrorMessage()).isEqualTo("10 : Error was caused by duplicate key exception");

        assertThat(ErrorConstants.INVALID_REQUEST_CCD.getErrorMessage()).isEqualTo("21 : There is a problem with your request. Please check and try again");

        assertThat(ErrorConstants.RESOURCE_NOT_FOUND.getErrorMessage()).isEqualTo("22 : Resource not found");

        assertThat(ErrorConstants.MISSING_TOKEN.getErrorMessage()).isEqualTo("23 : Missing Bearer Token");

        assertThat(ErrorConstants.ACCESS_EXCEPTION_CCD.getErrorMessage()).isEqualTo("24 : Access Denied");

        assertThat(ErrorConstants.USER_EXISTS.getErrorMessage()).isEqualTo("25 : User already exists");

        assertThat(ErrorConstants.UNKNOWN_EXCEPTION_CCD.getErrorMessage()).isEqualTo("26 : error was caused by an unknown exception");

    }

}
