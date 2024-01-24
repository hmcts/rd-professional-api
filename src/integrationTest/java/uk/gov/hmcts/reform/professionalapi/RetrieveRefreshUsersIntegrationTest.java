package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.domain.RefreshUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ISO_DATE_TIME_FORMATTER;

class RetrieveRefreshUsersIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    void retrieve_refresh_users_using_since_should_return_single_user_with_no_access_types_and_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);

        LocalDateTime dateTime = LocalDateTime.now();
        dateTime = dateTime.minusHours(1);
        String since = dateTime.format(ISO_DATE_TIME_FORMATTER);

        Map<String, Object> response = professionalReferenceDataClient.findRefreshUsersWithSince(since, 10);
        validateResponse(response, 1, 0);
    }

    @Test
    void retrieve_refresh_users_using_user_identifier_should_return_single_user_with_1_access_types_and_status_200() {
        updateUserProfileRolesMock(HttpStatus.OK);
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);
        String userIdentifier = retrieveSuperUserIdentifierFromOrganisationId(organisationIdentifier);

        UserProfileUpdatedData userProfileUpdatedData = createModifyUserConfiguredAccessData("test@mail.com", 1);

        professionalReferenceDataClient
                .modifyUserRolesOfOrganisationExternal(userProfileUpdatedData, userIdentifier, puiUserManager);

        Map<String, Object> response = professionalReferenceDataClient
                .findRefreshUsersWithUserIdentifier(userIdentifier);
        validateResponse(response, 1, 1);
    }

    @Test
    void retrieve_refresh_users_using_user_identifier_should_return_single_user_with_2_access_types_and_status_200() {
        updateUserProfileRolesMock(HttpStatus.OK);
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);
        String userIdentifier = retrieveSuperUserIdentifierFromOrganisationId(organisationIdentifier);

        UserProfileUpdatedData userProfileUpdatedData = createModifyUserConfiguredAccessData("test@mail.com", 2);

        professionalReferenceDataClient
                .modifyUserRolesOfOrganisationExternal(userProfileUpdatedData, userIdentifier, puiUserManager);

        Map<String, Object> response = professionalReferenceDataClient
                .findRefreshUsersWithUserIdentifier(userIdentifier);
        validateResponse(response, 1, 2);
    }

    @Test
    void retrieve_refresh_users_using_since_older_than_user_should_return_no_users_and_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);

        LocalDateTime dateTime = LocalDateTime.now();
        dateTime = dateTime.plusHours(1);
        String since = dateTime.format(ISO_DATE_TIME_FORMATTER);

        Map<String, Object> response = professionalReferenceDataClient.findRefreshUsersWithSince(since, 10);
        validateResponse(response, 0, 0);
    }

    @Test
    void retrieve_refresh_users_using_invalid_user_identifier_should_no_users_and_status_2000() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);

        Map<String, Object> response = professionalReferenceDataClient.findRefreshUsersWithUserIdentifier("123");
        validateResponse(response, 0, 0);
    }

    void validateResponse(Map<String, Object> response, int expectedUsers, int expectedUserAccessTypes) {
        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertNotNull(response.get("users"));
        assertThat((List<RefreshUser>) response.get("users")).size().isEqualTo(expectedUsers);

        List<HashMap> refreshUsers = (List<HashMap>) response.get("users");

        refreshUsers.forEach(user -> {
            assertNotNull(user.get(USER_IDENTIFIER));
            assertNotNull(user.get(LAST_UPDATED));
            assertNotNull(user.get(ORG_INFO));
            assertNotNull(user.get(USER_ACCESS_TYPES));
            assertNull(user.get(DATE_TIME_DELETED));

            List<LinkedHashMap> userAccessTypes = (List<LinkedHashMap>) user.get("userAccessTypes");

            assertThat(userAccessTypes).size().isEqualTo(expectedUserAccessTypes);
            userAccessTypes.forEach(userAccessType -> {
                assertNotNull(userAccessType.get("jurisdictionId"));
                assertNotNull(userAccessType.get("organisationProfileId"));
                assertNotNull(userAccessType.get("accessTypeId"));
                assertNotNull(userAccessType.get("enabled"));
            });
        });

        if (expectedUsers > 0) {
            assertNotNull(response.get("lastRecordInPage"));
        }
        assertNotNull(response.get("moreAvailable"));
    }
}
