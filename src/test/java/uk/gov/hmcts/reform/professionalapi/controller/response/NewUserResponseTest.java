package uk.gov.hmcts.reform.professionalapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;


public class NewUserResponseTest {

    @Test
    public void test_getUserIdentifier() {
        ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);
        when(professionalUserMock.getUserIdentifier()).thenReturn("userIdentifier");

        NewUserResponse newUserResponse = new NewUserResponse(professionalUserMock);

        when(newUserResponse.getUserIdentifier()).thenReturn("userIdentifier");

        assertThat(newUserResponse.getUserIdentifier()).isEqualTo("userIdentifier");
    }

    @Test
    public void test_userIdentifier_with_setter() {
        ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);
        when(professionalUserMock.getUserIdentifier()).thenReturn("userIdentifier");
        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setUserIdentifier("userIdentifier");
        assertThat(newUserResponse.getUserIdentifier()).isEqualTo("userIdentifier");
    }
}
