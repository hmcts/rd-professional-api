package uk.gov.hmcts.reform.professionalapi.controller.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.domain.LanguagePreference.EN;
import static uk.gov.hmcts.reform.professionalapi.domain.UserCategory.PROFESSIONAL;
import static uk.gov.hmcts.reform.professionalapi.domain.UserType.EXTERNAL;

@ExtendWith(MockitoExtension.class)
class UserProfileCreationRequestTest {

    private final String email = "test@test.com";
    private final String firstName = "fName";
    private final String lastName = "lName";

    @Test
    void test_has_mandatory_fields_specified_not_null() {
        List<String> roles = asList("pui-user-manager", "pui-organisation-manager");

        UserProfileCreationRequest userProfileCreationRequest = new UserProfileCreationRequest(email, firstName,
                lastName, EN, PROFESSIONAL, EXTERNAL, roles, false);

        userProfileCreationRequest.setEmail("test@test.com");

        assertThat(userProfileCreationRequest.getEmail()).isEqualTo("test@test.com");
        assertThat(userProfileCreationRequest.getFirstName()).isEqualTo(firstName);
        assertThat(userProfileCreationRequest.getLastName()).isEqualTo(lastName);
        assertThat(userProfileCreationRequest.getLanguagePreference()).isEqualTo(EN);
        assertThat(userProfileCreationRequest.getUserCategory()).isEqualTo(PROFESSIONAL);
        assertThat(userProfileCreationRequest.getUserType()).isEqualTo(EXTERNAL);
        assertThat(userProfileCreationRequest.getRoles()).hasSize(2);
        assertThat(userProfileCreationRequest.getRoles().get(0)).isEqualTo("pui-user-manager");
        assertThat(userProfileCreationRequest.getRoles().get(1)).isEqualTo("pui-organisation-manager");
    }

    @Test
    void test_BuilderMethod() {
        List<String> roles = asList("aRole");

        UserProfileCreationRequest userProfileCreationRequest
                = UserProfileCreationRequest.anUserProfileCreationRequest()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .languagePreference(EN)
                .userCategory(PROFESSIONAL)
                .userType(EXTERNAL)
                .roles(roles)
                .build();

        assertThat(userProfileCreationRequest.getEmail()).isEqualTo(email);
        assertThat(userProfileCreationRequest.getFirstName()).isEqualTo(firstName);
        assertThat(userProfileCreationRequest.getLastName()).isEqualTo(lastName);
        assertThat(userProfileCreationRequest.getLanguagePreference()).isEqualTo(EN);
        assertThat(userProfileCreationRequest.getUserCategory()).isEqualTo(PROFESSIONAL);
        assertThat(userProfileCreationRequest.getUserType()).isEqualTo(EXTERNAL);
        assertThat(userProfileCreationRequest.getRoles()).isEqualTo(roles);
    }

}