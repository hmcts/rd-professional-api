package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.Jurisdiction;

public class NewUserCreationRequestTest {

    @Test
    public void testCreatesNewUser() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        Jurisdiction jurisdiction = new Jurisdiction();
        List<Jurisdiction> jurisdictions = new ArrayList<>();
        jurisdictions.add(jurisdiction);

        NewUserCreationRequest newUserCreationRequest =
                new NewUserCreationRequest("some-name", "some-last-name",
                        "some@email.com",  userRoles, jurisdictions, false);

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

        Jurisdiction jurisdiction = new Jurisdiction();
        List<Jurisdiction> jurisdictions = new ArrayList<>();
        jurisdictions.add(jurisdiction);

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