package uk.gov.hmcts.reform.professionalapi.controller;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorConstantsTest {

    @Test
    public void check_constants(){

        assertThat(ErrorConstants.MALFORMED_JSON.equals("Malformed Input Request"));

        assertThat(ErrorConstants.UNSUPPORTED_MEDIA_TYPES.equals("Unsupported Media Types"));

        assertThat(ErrorConstants.INVALID_REQUEST.equals("There is a problem with your request. Please check and try again"));




    }

}
