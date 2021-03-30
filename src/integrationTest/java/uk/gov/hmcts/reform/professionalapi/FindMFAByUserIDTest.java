package uk.gov.hmcts.reform.professionalapi;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
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
import static uk.gov.hmcts.reform.professionalapi.util.FeatureConditionEvaluation.FORBIDDEN_EXCEPTION_LD;

@Slf4j
public class FindMFAByUserIDTest extends AuthorizationEnabledIntegrationTest {


    @Test
    public void get_request_with_valid_user_id_returns_mfa_status() {
        Map<String, Object> response = createOrganization();

        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(response.get("mfa")).isNotNull();
        assertThat(response.get("mfa")).isEqualTo("EMAIL");
    }

    @Test
    public void returns_404_when_user_identifier_not_found() {
        Map<String, Object> response = professionalReferenceDataClient.findMFAByUserID(UUID.randomUUID().toString());
        assertThat(response.get("http_status")).isEqualTo("404");
        assertThat(response.get("response_body").toString()).contains("The requested user does not exist");
    }

    @Test
    public void returns_400_when_organisation_not_active() {
        String pendingOrganisationId = createOrganisationRequest();
        updateOrganisation(pendingOrganisationId, hmctsAdmin, "PENDING");
        Organisation pendingOrganisation = organisationRepository.findByOrganisationIdentifier(pendingOrganisationId);


        ProfessionalUser superUser = new ProfessionalUser("some-fname", "some-lname",
                "soMeone@somewhere.com", pendingOrganisation);
        superUser.setUserIdentifier(UUID.randomUUID().toString());
        professionalUserRepository.save(superUser);

        Map<String, Object> response = professionalReferenceDataClient.findMFAByUserID(superUser
                .getUserIdentifier());

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body").toString())
            .contains("The requested user's organisation is not 'Active'");
    }

    @Test
    public void returns_400_when_user_id_not_present() {
        Map<String, Object> response = professionalReferenceDataClient.findMFAByUserID(StringUtils.EMPTY);
        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body").toString()).contains("User Id cannot be empty");
    }

    @Test
    public void returns_LaunchDarkly_Forbidden_when_retrieve_mfa_status_with_invalid_flag() {
        Map<String, String> launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("OrganisationMfaStatusController.retrieveMfaStatusByUserId",
                "test-get-mfa-flag");
        when(featureToggleService.isFlagEnabled(anyString(), anyString())).thenReturn(false);
        when(featureToggleService.getLaunchDarklyMap()).thenReturn(launchDarklyMap);
        Map<String, Object> errorResponseMap = createOrganization();

        assertThat(errorResponseMap.get("http_status")).isEqualTo("403");
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
