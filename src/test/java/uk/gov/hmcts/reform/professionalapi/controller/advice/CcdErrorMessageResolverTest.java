package uk.gov.hmcts.reform.professionalapi.controller.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.CcdErrorMessageResolver.resolveStatusAndReturnMessage;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.ACCESS_EXCEPTION_CCD;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.INVALID_REQUEST_CCD;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.MISSING_TOKEN;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.RESOURCE_NOT_FOUND;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.UNKNOWN_EXCEPTION_CCD;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.USER_EXISTS;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

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

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<CcdErrorMessageResolver> constructor = CcdErrorMessageResolver.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}
