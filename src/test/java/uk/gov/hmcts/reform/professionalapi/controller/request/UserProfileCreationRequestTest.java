package uk.gov.hmcts.reform.professionalapi.controller.request;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.domain.LanguagePreference.EN;
import static uk.gov.hmcts.reform.professionalapi.domain.UserCategory.PROFESSIONAL;
import static uk.gov.hmcts.reform.professionalapi.domain.UserType.EXTERNAL;

import java.util.List;
import org.junit.Test;

public class UserProfileCreationRequestTest {

    private String email = "some@hmcts.net";
    private String firstName = "fName";
    private String lastName = "lName";

    @Test
    public void test_has_mandatory_fields_specified_not_null() {
        List<String> roles = asList("pui-user-manager", "pui-organisation-manager");

        UserProfileCreationRequest userProfileCreationRequest = new UserProfileCreationRequest(email, firstName,
                lastName, EN, PROFESSIONAL, EXTERNAL, roles, false);

        userProfileCreationRequest.setEmail("somebody@hmcts.net");

        assertThat(userProfileCreationRequest.getEmail()).isEqualTo("somebody@hmcts.net");
        assertThat(userProfileCreationRequest.getFirstName()).isEqualTo(firstName);
        assertThat(userProfileCreationRequest.getLastName()).isEqualTo(lastName);
        assertThat(userProfileCreationRequest.getLanguagePreference()).isEqualTo(EN);
        assertThat(userProfileCreationRequest.getUserCategory()).isEqualTo(PROFESSIONAL);
        assertThat(userProfileCreationRequest.getUserType()).isEqualTo(EXTERNAL);
        assertThat(userProfileCreationRequest.getRoles().size()).isEqualTo(2);
        assertThat(userProfileCreationRequest.getRoles().get(0)).isEqualTo("pui-user-manager");
        assertThat(userProfileCreationRequest.getRoles().get(1)).isEqualTo("pui-organisation-manager");
    }

    @Test
    public void test_BuilderMethod() {
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