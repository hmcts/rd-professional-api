package uk.gov.hmcts.reform.professionalapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.advice.GetUserProfileException;

public class GetUserProfileExceptionTest {

    @Test
    public void getUserProfileExceptionTest() {

        GetUserProfileException getUserProfileException = new GetUserProfileException(BAD_REQUEST, "BAD REQUEST");

        assertThat(getUserProfileException.getHttpStatus().toString()).isEqualTo("400 BAD_REQUEST");
        assertThat(getUserProfileException.getErrorMessage()).isEqualTo("BAD REQUEST");

    }


}
