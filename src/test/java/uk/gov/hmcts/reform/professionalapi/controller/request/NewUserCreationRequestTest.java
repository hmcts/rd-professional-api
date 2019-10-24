package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Test;

public class NewUserCreationRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void testCreatesNewUser() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        Jurisdiction jurisdictionMock = mock(Jurisdiction.class);
        List<Jurisdiction> jurisdictions = new ArrayList<>();
        jurisdictions.add(jurisdictionMock);

        NewUserCreationRequest newUserCreationRequest =
                new NewUserCreationRequest("some-name", "some-last-name", "some@email.com",  userRoles, jurisdictions);

        assertThat(newUserCreationRequest.getFirstName()).isEqualTo("some-name");
        assertThat(newUserCreationRequest.getLastName()).isEqualTo("some-last-name");
        assertThat(newUserCreationRequest.getEmail()).isEqualTo("some@email.com");
        assertThat(newUserCreationRequest.getRoles()).hasSize(1);
        assertThat(newUserCreationRequest.getJurisdictions()).isEqualTo(jurisdictions);
    }


    @Test
    public void newUserCreationBuilderTest() {
        String testFirstName = "Jane";
        String testLastName = "Doe";
        String testEmail = "jane.doe@email.com";
        List<String> testRoles = new ArrayList<>();
        testRoles.add("a role");

        Jurisdiction jurisdictionMock = mock(Jurisdiction.class);
        List<Jurisdiction> jurisdictions = new ArrayList<>();
        jurisdictions.add(jurisdictionMock);

        NewUserCreationRequest testNewUserCreationRequest = NewUserCreationRequest.aNewUserCreationRequest()
                .firstName(testFirstName)
                .lastName(testLastName)
                .email(testEmail)
                .roles(testRoles)
                .jurisdictions(jurisdictions)
                .build();

        assertThat(testNewUserCreationRequest.getFirstName()).isEqualTo(testFirstName);
        assertThat(testNewUserCreationRequest.getLastName()).isEqualTo(testLastName);
        assertThat(testNewUserCreationRequest.getEmail()).isEqualTo(testEmail);
        assertThat(testNewUserCreationRequest.getRoles()).isEqualTo(testRoles);
        assertThat(testNewUserCreationRequest.getJurisdictions()).isEqualTo(jurisdictions);
    }
}