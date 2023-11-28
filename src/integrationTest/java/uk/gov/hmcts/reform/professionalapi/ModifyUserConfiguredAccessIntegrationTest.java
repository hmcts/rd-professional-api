package uk.gov.hmcts.reform.professionalapi;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.AccessType;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;


@Slf4j
class ModifyUserConfiguredAccessIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    void ac1_modify_user_configured_access_for_an_active_organisation_with_prd_admin_role_should_return_200() {

        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        List<String> userRoles = new ArrayList<>();
        userRoles.add(puiCaseManager);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        updateUserProfileRolesMock(HttpStatus.OK);

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier,
                        inviteUserCreationRequest(randomAlphabetic(5) + "@email.com", userRoles),
                        hmctsAdmin);

        String userIdentifier = (String) newUserResponse.get(USER_IDENTIFIER);
        UserProfileUpdatedData userProfileUpdatedData = createModifyUserConfiguredAccessData();

        Map<String, Object> response = professionalReferenceDataClient
                .modifyUserConfiguredAcessOfOrganisation(userProfileUpdatedData, organisationIdentifier, userIdentifier,
                        hmctsAdmin);
        assertThat(response.get("http_status")).isNotNull();
        assertThat(response.get("http_status")).isEqualTo("200 OK");

    }

    private UserProfileUpdatedData createModifyUserConfiguredAccessData() {

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        AccessType accessType1 = new AccessType();
        AccessType accessType2 = new AccessType();
        Set<AccessType> accessTypes = new HashSet<>();
        accessTypes.add(accessType1);
        accessTypes.add(accessType2);

        userProfileUpdatedData.setAccessTypes(accessTypes);
        userProfileUpdatedData.setIdamStatus(IdamStatus.ACTIVE.name());
        userProfileUpdatedData.setRolesAdd(new HashSet<>());
        userProfileUpdatedData.setRolesDelete(new HashSet<>());
        return userProfileUpdatedData;
    }


}
