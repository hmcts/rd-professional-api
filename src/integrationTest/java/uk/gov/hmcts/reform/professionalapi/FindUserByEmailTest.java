package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

public class FindUserByEmailTest extends AuthorizationEnabledIntegrationTest {

    @Before
    public void setUp() {
        Organisation organisation = new Organisation("some-org-name", null, "PENDING", null, null, null);
        ProfessionalUser superUser = new ProfessionalUser("some-fname", "some-lname", "someone@somewhere.com", organisation);
        organisationRepository.save(organisation);
        professionalUserRepository.save(superUser);
    }


    @Test
    public void search_returns_valid_user_with_organisation_status_as_active() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisation(organisationIdentifier,"True", hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(((List<ProfessionalUsersResponse>) response.get("users")).size()).isGreaterThan(0);

        List<HashMap> professionalUsersResponses = (List<HashMap>) response.get("users");
        HashMap professionalUsersResponse = professionalUsersResponses.get(0);

        assertThat(professionalUsersResponse.get("userIdentifier")).isNotNull();
        assertThat(professionalUsersResponse.get("firstName")).isNotNull();
        assertThat(professionalUsersResponse.get("lastName")).isNotNull();
        assertThat(professionalUsersResponse.get("email")).isNotNull();
        assertThat(((List)professionalUsersResponse.get("roles")).size()).isEqualTo(1);
    }

    @Test
    public void returns_404_when_email_not_found() {
        Map<String, Object> response =
                professionalReferenceDataClient.findUserByEmail("someone@nowhere.com", hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("404");
    }


    @Test
    public void returns_404_when_organisation_status_is_not_active() {
        Map<String, Object> response =
                professionalReferenceDataClient.findUserByEmail("someone@somewhere.com", hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("404");
    }
}
