package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.professionalapi.domain.UserCategory;
import uk.gov.hmcts.reform.professionalapi.domain.UserType;


public class UserProfileCreationRequestTest {

    @Test
    public void has_mandatory_fields_specified_not_null() {
        List<String> roles = new ArrayList<String>();
        roles.add("pui-user-manager");
        roles.add("pui-organisation-manager");
        UserProfileCreationRequest userProfileCreationRequest =
                new UserProfileCreationRequest("some@hmcts.net", "fname", "lname", LanguagePreference.EN, UserCategory.PROFESSIONAL, UserType.EXTERNAL, roles);

           userProfileCreationRequest.setEmail("somebody@hmcts.net");

        assertThat(userProfileCreationRequest.getEmail()).isEqualTo("somebody@hmcts.net");
        assertThat(userProfileCreationRequest.getFirstName()).isEqualTo("fname");
        assertThat(userProfileCreationRequest.getLastName()).isEqualTo("lname");
        assertThat(userProfileCreationRequest.getLanguagePreference()).isEqualTo(LanguagePreference.EN);
        assertThat(userProfileCreationRequest.getUserCategory()).isEqualTo(UserCategory.PROFESSIONAL);
        assertThat(userProfileCreationRequest.getUserType()).isEqualTo(UserType.EXTERNAL);
        assertThat(userProfileCreationRequest.getRoles().size()).isEqualTo(2);
    }

}