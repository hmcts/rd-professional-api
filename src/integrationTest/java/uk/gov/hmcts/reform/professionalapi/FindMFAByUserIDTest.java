package uk.gov.hmcts.reform.professionalapi;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.EMPTY_USER_ID;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.NO_USER_FOUND;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_NOT_ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.util.FeatureConditionEvaluation.FORBIDDEN_EXCEPTION_LD;

@Slf4j
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class FindMFAByUserIDTest extends AuthorizationEnabledIntegrationTest {


    @Test
    void get_request_with_valid_user_id_returns_mfa_status() {
        Map<String, Object> response = createOrganization();

        assertThat(response).containsEntry("http_status", "200 OK");
        assertThat(response.get("mfa")).isNotNull();
        assertThat(response).containsEntry("mfa", "EMAIL");
    }

    @Test
    void returns_404_when_user_identifier_not_found() {
        Map<String, Object> response = professionalReferenceDataClient.findMFAByUserID(UUID.randomUUID().toString());
        assertThat(response).containsEntry("http_status", "404");
        assertThat(response.get("response_body").toString()).contains(NO_USER_FOUND);
    }

    @Test
    void returns_400_when_organisation_not_active() {
        String pendingOrganisationId = createOrganisationRequest();
        updateOrganisation(pendingOrganisationId, hmctsAdmin, "PENDING");
        Organisation pendingOrganisation = organisationRepository.findByOrganisationIdentifier(pendingOrganisationId);


        ProfessionalUser superUser = new ProfessionalUser("some-fname", "some-lname",
                "soMeone@somewhere.com", pendingOrganisation);
        superUser.setUserIdentifier(UUID.randomUUID().toString());
        professionalUserRepository.save(superUser);

        Map<String, Object> response = professionalReferenceDataClient.findMFAByUserID(superUser
                .getUserIdentifier());

        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString()).contains(ORG_NOT_ACTIVE);
    }

    @Test
    void returns_400_when_user_id_not_present() {
        Map<String, Object> response = professionalReferenceDataClient.findMFAByUserID(StringUtils.EMPTY);
        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString()).contains(EMPTY_USER_ID);
    }

    @Test
    void returns_LaunchDarkly_Forbidden_when_retrieve_mfa_status_with_invalid_flag() {
        Map<String, String> launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("OrganisationMfaStatusController.retrieveMfaStatusByUserId",
                "test-get-mfa-flag");
        when(featureToggleService.isFlagEnabled(anyString(), anyString())).thenReturn(false);
        when(featureToggleService.getLaunchDarklyMap()).thenReturn(launchDarklyMap);
        Map<String, Object> errorResponseMap = createOrganization();

        assertThat(errorResponseMap).containsEntry("http_status", "403");
        assertThat((String) errorResponseMap.get("response_body"))
                .contains("test-get-mfa-flag".concat(SPACE).concat(FORBIDDEN_EXCEPTION_LD));
    }

    private Map<String, Object> createOrganization() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(organisationIdentifier);
        SuperUser persistedSuperUser = persistedOrganisation.getUsers().get(0);

        return professionalReferenceDataClient.findMFAByUserID(persistedSuperUser.getUserIdentifier());
    }
    
}
