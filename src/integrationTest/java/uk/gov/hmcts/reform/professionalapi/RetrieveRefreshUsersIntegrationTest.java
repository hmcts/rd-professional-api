package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.domain.AccessType;
import uk.gov.hmcts.reform.professionalapi.domain.RefreshUser;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.SINCE_TIMESTAMP_FORMAT;

class RetrieveRefreshUsersIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    void retrieve_refresh_users_using_since_should_return_single_user_and_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        LocalDateTime dateTime = LocalDateTime.now();
        dateTime = dateTime.plusHours(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SINCE_TIMESTAMP_FORMAT);
        String since = dateTime.format(formatter);

        Map<String, Object> response = professionalReferenceDataClient.findRefreshUsersWithSince(since, 0, 10);

        validateResponse(response, 1);
    }

    @Test
    void retrieve_refresh_users_using_user_identifier_should_return_single_user_and_status_2000() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");
        String userIdentifier = retrieveSuperUserIdentifierFromOrganisationId(organisationIdentifier);

        Map<String, Object> response = professionalReferenceDataClient.findRefreshUsersWithUserIdentifier(userIdentifier, 0, 10);
        validateResponse(response, 1);
    }

    @Test
    void retrieve_refresh_users_using_since_should_return_no_users_and_status_200() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SINCE_TIMESTAMP_FORMAT);
        String since = dateTime.format(formatter);

        Map<String, Object> response = professionalReferenceDataClient.findRefreshUsersWithSince(since, 0, 10);

        validateResponse(response, 0);
    }

    @Test
    void retrieve_refresh_users_using_user_identifier_should_no_users_and_status_2000() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        Map<String, Object> response = professionalReferenceDataClient.findRefreshUsersWithUserIdentifier("123", 0, 10);
        validateResponse(response, 0);
    }

    void validateResponse(Map<String, Object> response, int expectedUserCount) {
        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertNotNull(response.get("users"));
        assertThat((List<RefreshUser>) response.get("users")).size().isEqualTo(expectedUserCount);

        List<HashMap> refreshUsers = (List<HashMap>) response.get("users");

        refreshUsers.forEach(user -> {
            assertNotNull(user.get(USER_IDENTIFIER));
            assertNotNull(user.get(LAST_UPDATED));
            assertNotNull(user.get(ORG_IDENTIFIER));
            assertNotNull(user.get(ACCESS_TYPES));

            List<AccessType> accessTypes = (List<AccessType>) user.get("accessTypes");
            accessTypes.forEach(accessType -> {
                assertNotNull(accessType.getJurisdictionId());
                assertNotNull(accessType.getOrganisationProfileId());
                assertNotNull(accessType.getAccessTypeId());
                assertNotNull(accessType.isEnabled());
            });
        });

        assertNotNull(response.get("moreAvailable"));
    }
}
