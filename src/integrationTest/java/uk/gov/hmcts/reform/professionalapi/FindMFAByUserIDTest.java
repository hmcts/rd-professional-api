package uk.gov.hmcts.reform.professionalapi;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;

@Slf4j
public class FindMFAByUserIDTest extends AuthorizationEnabledIntegrationTest {

    String organisationIdentifier;
    String pendingOrganisationId;

    @Before
    public void setUp(){
        organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");
    }

    @Test
    public void get_request_with_valid_user_id_returns_mfa_status() {

        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(organisationIdentifier);
        SuperUser persistedSuperUser = persistedOrganisation.getUsers().get(0);

        Map<String, Object> response = professionalReferenceDataClient.findMFAByUserID(persistedSuperUser
                .getUserIdentifier());

        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(response.get("mfa")).isNotNull();
        assertThat(response.get("mfa")).isEqualTo("EMAIL");
    }

    @Test
    public void returns_404_when_user_identifier_not_found() {

        Map<String, Object> response = professionalReferenceDataClient.findMFAByUserID(UUID.randomUUID().toString());
        assertThat(response.get("http_status")).isEqualTo("404");
        assertThat(response.get("errorDescription")).isEqualTo("The requested user does not exist");
    }

    @Test
    public void returns_404_when_organisation_not_active() {

        pendingOrganisationId = createOrganisationRequest();
        updateOrganisation(pendingOrganisationId, hmctsAdmin, "PENDING");
        Organisation ppOrganisation = organisationRepository
                .findByOrganisationIdentifier(pendingOrganisationId);

        SuperUser persistedSuperUser = ppOrganisation.getUsers().get(0);

        Map<String, Object> response = professionalReferenceDataClient.findMFAByUserID(persistedSuperUser
                .getUserIdentifier());

        assertThat(response.get("http_status")).isEqualTo("404");
        assertThat(response.get("errorDescription")).isEqualTo("The requested user's organisation is not 'Active'");
    }

    @Test
    public void returns_400_when_user_id_not_present() {

        Map<String, Object> response = professionalReferenceDataClient.findMFAByUserID(StringUtils.EMPTY);
        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body").toString()).contains("Bad Request");
    }

    private String createPendingOrganisationRequest() {
        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().status("PENDING").build();
        Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest);
        return (String) responseForOrganisationCreation.get(ORG_IDENTIFIER);
    }



}
