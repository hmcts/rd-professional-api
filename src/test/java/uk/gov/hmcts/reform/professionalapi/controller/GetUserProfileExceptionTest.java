package uk.gov.hmcts.reform.professionalapi.controller;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.advice.GetUserProfileException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class GetUserProfileExceptionTest {

    @Test
    public void getUserProfileExceptionTest() {

        GetUserProfileException getUserProfileException = new GetUserProfileException(BAD_REQUEST, "BAD REQUEST");

        assertThat(getUserProfileException.getHttpStatus().toString()).isEqualTo("400 BAD_REQUEST");
        assertThat(getUserProfileException.getErrorMessage()).isEqualTo("BAD REQUEST");

    }


}
