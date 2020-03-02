package uk.gov.hmcts.reform.professionalapi.controller.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ErrorConstantsTest {

    @Test
    public void check_constants() {

        assertThat(ErrorConstants.MALFORMED_JSON.getErrorMessage()).isEqualTo("1 : Malformed Input Request");

        assertThat(ErrorConstants.UNSUPPORTED_MEDIA_TYPES.getErrorMessage()).isEqualTo("2 : Unsupported Media Type");

        assertThat(ErrorConstants.INVALID_REQUEST.getErrorMessage()).isEqualTo("3 : There is a problem with your request. Please check and try again");

    }

}
