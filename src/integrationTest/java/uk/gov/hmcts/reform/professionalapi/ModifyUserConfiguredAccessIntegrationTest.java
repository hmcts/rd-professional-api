package uk.gov.hmcts.reform.professionalapi;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.Map;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
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
}
