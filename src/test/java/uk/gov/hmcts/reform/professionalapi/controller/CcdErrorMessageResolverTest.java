package uk.gov.hmcts.reform.professionalapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.CcdErrorMessageResolver.resolveStatusAndReturnMessage;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.ACCESS_EXCEPTION_CCD;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.INVALID_REQUEST_CCD;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.MISSING_TOKEN;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.RESOURCE_NOT_FOUND;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.UNKNOWN_EXCEPTION_CCD;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.USER_EXISTS;

import org.junit.Test;
import org.springframework.http.HttpStatus;

public class CcdErrorMessageResolverTest {

    @Test
    public void should_return_error_message_by_HttpStatus_provided() {
        String httpStatusString;

        httpStatusString = resolveStatusAndReturnMessage(HttpStatus.BAD_REQUEST);
        assertThat(httpStatusString).isEqualTo(INVALID_REQUEST_CCD.getErrorMessage());

        httpStatusString = resolveStatusAndReturnMessage(HttpStatus.NOT_FOUND);
        assertThat(httpStatusString).isEqualTo(RESOURCE_NOT_FOUND.getErrorMessage());

        httpStatusString = resolveStatusAndReturnMessage(HttpStatus.UNAUTHORIZED);
        assertThat(httpStatusString).isEqualTo(MISSING_TOKEN.getErrorMessage());

        httpStatusString = resolveStatusAndReturnMessage(HttpStatus.FORBIDDEN);
        assertThat(httpStatusString).isEqualTo(ACCESS_EXCEPTION_CCD.getErrorMessage());

        httpStatusString = resolveStatusAndReturnMessage(HttpStatus.CONFLICT);
        assertThat(httpStatusString).isEqualTo(USER_EXISTS.getErrorMessage());

        httpStatusString = resolveStatusAndReturnMessage(HttpStatus.MULTI_STATUS);
        assertThat(httpStatusString).isEqualTo(UNKNOWN_EXCEPTION_CCD.getErrorMessage());
    }
}
