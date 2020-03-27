package uk.gov.hmcts.reform.professionalapi.controller.request;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.PUI_ORGANISATION_MANAGER;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.PUI_USER_MANAGER;
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
    public void has_mandatory_fields_specified_not_null() {
        List<String> roles = asList(PUI_USER_MANAGER, PUI_ORGANISATION_MANAGER);
        UserProfileCreationRequest userProfileCreationRequest = new UserProfileCreationRequest(email, firstName, lastName, EN, PROFESSIONAL, EXTERNAL, roles);

        userProfileCreationRequest.setEmail("somebody@hmcts.net");

        assertThat(userProfileCreationRequest.getEmail()).isEqualTo("somebody@hmcts.net");
        assertThat(userProfileCreationRequest.getFirstName()).isEqualTo(firstName);
        assertThat(userProfileCreationRequest.getLastName()).isEqualTo(lastName);
        assertThat(userProfileCreationRequest.getLanguagePreference()).isEqualTo(EN);
        assertThat(userProfileCreationRequest.getUserCategory()).isEqualTo(PROFESSIONAL);
        assertThat(userProfileCreationRequest.getUserType()).isEqualTo(EXTERNAL);
        assertThat(userProfileCreationRequest.getRoles().size()).isEqualTo(2);
        assertThat(userProfileCreationRequest.getRoles().get(0)).isEqualTo(PUI_USER_MANAGER);
        assertThat(userProfileCreationRequest.getRoles().get(1)).isEqualTo(PUI_ORGANISATION_MANAGER);
    }

    @Test
    public void test_BuilderMethod() {
        List<String> roles = asList("aRole");

        UserProfileCreationRequest userProfileCreationRequest = UserProfileCreationRequest.anUserProfileCreationRequest()
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