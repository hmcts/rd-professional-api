package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;

public class SuperUserResponseTest {

    @Test
    public void test_SuperUserResponse() {
        Organisation organisation = mock(Organisation.class);

        SuperUser superUser = new SuperUser("some-fname", "some-lname", "some-email-address", organisation);

        SuperUserResponse superUserResponse = new SuperUserResponse(superUser);

        assertThat(superUserResponse).isNotNull();
    }
}
