package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NewUserCreationRequestTest {

    @Test
    void test_CreatesNewUser() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");


        NewUserCreationRequest newUserCreationRequest =
                new NewUserCreationRequest("some-name", "some-last-name",
                        "some@email.com",  userRoles, false);

        assertThat(newUserCreationRequest.getFirstName()).isEqualTo("some-name");
        assertThat(newUserCreationRequest.getLastName()).isEqualTo("some-last-name");
        assertThat(newUserCreationRequest.getEmail()).isEqualTo("some@email.com");
        assertThat(newUserCreationRequest.getRoles()).hasSize(1);
    }


    @Test
    void test_newUserCreationBuilder() {
        String testFirstName = "Jane";
        String testLastName = "Doe";
        String testEmail = "jane.doe@email.com";
        List<String> testRoles = new ArrayList<>();
        testRoles.add("a role");


        NewUserCreationRequest testNewUserCreationRequest = NewUserCreationRequest.aNewUserCreationRequest()
                .firstName(testFirstName)
                .lastName(testLastName)
                .email(testEmail)
                .roles(testRoles)
                .build();

        assertThat(testNewUserCreationRequest.getFirstName()).isEqualTo(testFirstName);
        assertThat(testNewUserCreationRequest.getLastName()).isEqualTo(testLastName);
        assertThat(testNewUserCreationRequest.getEmail()).isEqualTo(testEmail);
        assertThat(testNewUserCreationRequest.getRoles()).isEqualTo(testRoles);
    }
}