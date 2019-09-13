package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class UserRolesResponseTest {

    @Test
    public void should_Return_User_profile_Resposne() {

        UserRolesResponse userProfileRolesResponse = new UserRolesResponse("200", "Success");
        assertThat(userProfileRolesResponse.getStatusCode()).isEqualTo("200");
        assertThat(userProfileRolesResponse.getStatusMessage()).isEqualTo("Success");

    }
}