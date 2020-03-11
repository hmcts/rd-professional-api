package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;

public class SuperUserResponseTest {

    @Test
    public void test_SuperUserResponse() {
        SuperUserResponse superUserResponse = new SuperUserResponse(new SuperUser("some-fname", "some-lname", "some-email-address", null));
        assertThat(superUserResponse).isNotNull();
    }
}
