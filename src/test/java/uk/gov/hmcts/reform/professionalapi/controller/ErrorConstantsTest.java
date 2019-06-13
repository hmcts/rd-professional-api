package uk.gov.hmcts.reform.professionalapi.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants;

public class ErrorConstantsTest {

    @Test
    public void check_constants() {

        assertThat(ErrorConstants.MALFORMED_JSON.equals("1 : Malformed Input Request"));

        assertThat(ErrorConstants.UNSUPPORTED_MEDIA_TYPES.equals("2 : Unsupported Media Type"));

        assertThat(ErrorConstants.INVALID_REQUEST.equals("There is a problem with your request. Please check and try again"));

    }

}
