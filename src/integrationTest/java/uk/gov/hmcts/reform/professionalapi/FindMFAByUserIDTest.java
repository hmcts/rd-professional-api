package uk.gov.hmcts.reform.professionalapi;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class FindMFAByUserIDTest extends AuthorizationEnabledIntegrationTest {


    @Test
    public void get_request_with_valid_user_id_returns_mfa_status() {
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

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
        assertThat(response.get("response_body").toString()).contains("The requested user does not exist");
    }

    @Test
    public void returns_404_when_organisation_not_active() {

        String pendingOrganisationId = createOrganisationRequest();
        updateOrganisation(pendingOrganisationId, hmctsAdmin, "PENDING");
        Organisation pendingOrganisation = organisationRepository.findByOrganisationIdentifier(pendingOrganisationId);


        ProfessionalUser superUser = new ProfessionalUser("some-fname", "some-lname",
                "soMeone@somewhere.com", pendingOrganisation);
        superUser.setUserIdentifier(UUID.randomUUID().toString());
        professionalUserRepository.save(superUser);

        Map<String, Object> response = professionalReferenceDataClient.findMFAByUserID(superUser
                .getUserIdentifier());

        assertThat(response.get("http_status")).isEqualTo("404");
        assertThat(response.get("response_body").toString()).contains("The requested user's organisation is not 'Active'");
    }

    @Test
    public void returns_400_when_user_id_not_present() {

        Map<String, Object> response = professionalReferenceDataClient.findMFAByUserID(StringUtils.EMPTY);
        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body").toString()).contains("Invalid user id");
    }
    
}
