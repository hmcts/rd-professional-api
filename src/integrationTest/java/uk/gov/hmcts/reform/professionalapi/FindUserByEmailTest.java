package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;

public class FindUserByEmailTest extends Service2ServiceEnabledIntegrationTest {

    @Before
    public void setUp() {
        Organisation organisation = new Organisation("some-org-name", null, "PENDING", null, null, null);
        ProfessionalUser superUser = new ProfessionalUser("some-fname", "some-lname", "someone@somewhere.com", ProfessionalUserStatus.PENDING, organisation);
        organisationRepository.save(organisation);
        professionalUserRepository.save(superUser);
    }

    @Test
    public void search_returns_valid_user_with_organisation_status_as_active() {
        Organisation activeOrganisation = new Organisation("some-active-org", OrganisationStatus.ACTIVE, "sraid", null, null, null);
        ProfessionalUser activeSuperUser = new ProfessionalUser("some-fname", "some-lname", "activesomeone@here.com", ProfessionalUserStatus.PENDING, activeOrganisation);
        organisationRepository.save(activeOrganisation);
        professionalUserRepository.save(activeSuperUser);

        Map<String, Object> response =
                professionalReferenceDataClient.findUserByEmail("activesomeone@here.com");

        assertEquals("some-fname", response.get("firstName"));
        assertEquals("some-lname", response.get("lastName"));
        assertEquals("activesomeone@here.com", response.get("email"));
    }

    @Test
    public void returns_404_when_email_not_found() {
        Map<String, Object> response =
                professionalReferenceDataClient.findUserByEmail("someone@nowhere.com");

        assertThat(response.get("http_status")).isEqualTo("404");
    }

    @Test
    public void returns_404_when_organisation_status_is_not_active() {
        Map<String, Object> response =
                professionalReferenceDataClient.findUserByEmail("someone@somewhere.com");

        assertThat(response.get("http_status")).isEqualTo("404");
    }
}
