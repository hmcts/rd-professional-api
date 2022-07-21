package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SuperUserResponseTest {

    @Test
    void test_SuperUserResponse() {
        SuperUserResponse superUserResponse = new SuperUserResponse(new SuperUser("some-fname",
                "some-lname", "some-email-address", null));
        assertThat(superUserResponse).isNotNull();
    }
}
