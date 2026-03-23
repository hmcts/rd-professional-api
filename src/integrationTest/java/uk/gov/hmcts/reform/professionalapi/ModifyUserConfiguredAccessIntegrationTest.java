package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.time.LocalDateTime;
import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;

class ModifyUserConfiguredAccessIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    void ac1_modify_user_configured_access_for_an_active_organisation_with_puiUserManager_role_should_return_200() {

        updateUserProfileRolesMock(HttpStatus.OK);
        String email = randomAlphabetic(5) + "@email.com";
        UserProfileUpdatedData userProfileUpdatedData = createModifyUserConfiguredAccessData(email, 0);
        String userIdentifier = settingUpOrganisation(puiUserManager);
        Map<String, Object> response = professionalReferenceDataClient
                .modifyUserRolesOfOrganisationExternal(userProfileUpdatedData, userIdentifier, puiUserManager);
        assertThat(response.get("http_status")).isNotNull();
        assertThat(response.get("http_status")).isEqualTo("200 OK");
    }

    @Test
    void ac2_modify_user_configured_access_for_an_active_organisation_with_puiUserManager_role_should_return_200() {

        updateUserProfileRolesMock(HttpStatus.OK);
        String email = randomAlphabetic(5) + "@email.com";
        UserProfileUpdatedData userProfileUpdatedData = createModifyUserConfiguredAccessData(email, 2);
        String userIdentifier = settingUpOrganisation(puiUserManager);
        Map<String, Object> response = professionalReferenceDataClient
                .modifyUserRolesOfOrganisationExternal(userProfileUpdatedData, userIdentifier, puiUserManager);
        assertThat(response.get("http_status")).isNotNull();
        assertThat(response.get("http_status")).isEqualTo("200 OK");
    }

    @Test
    void should_update_last_updated_when_modifying_user_configured_access_external() throws Exception {
        updateUserProfileRolesMock(HttpStatus.OK);
        String userIdentifier = settingUpOrganisation(puiUserManager);

        ProfessionalUser before = professionalUserRepository.findByUserIdentifier(userIdentifier);
        assertThat(before).isNotNull();
        LocalDateTime beforeUpdated = before.getLastUpdated();
        assertThat(beforeUpdated).isNotNull();

        Thread.sleep(1100);

        String email = randomAlphabetic(5) + "@email.com";
        UserProfileUpdatedData userProfileUpdatedData = createModifyUserConfiguredAccessData(email, 1);
        Map<String, Object> response = professionalReferenceDataClient
                .modifyUserRolesOfOrganisationExternal(userProfileUpdatedData, userIdentifier, puiUserManager);
        assertThat(response.get("http_status")).isNotNull();
        assertThat(response.get("http_status")).isEqualTo("200 OK");

        ProfessionalUser after = professionalUserRepository.findByUserIdentifier(userIdentifier);
        assertThat(after.getLastUpdated()).isAfter(beforeUpdated);
    }
}
