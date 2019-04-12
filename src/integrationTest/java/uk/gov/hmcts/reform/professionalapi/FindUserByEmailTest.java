package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import uk.gov.hmcts.reform.professionalapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.entities.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.util.ProfessionalReferenceDataClient;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;

public class FindUserByEmailTest extends Service2ServiceEnabledIntegrationTest {

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private ProfessionalUserRepository professionalUserRepository;

    private ProfessionalReferenceDataClient professionalReferenceDataClient;

    @Before
    public void setUp() {
        professionalReferenceDataClient = new ProfessionalReferenceDataClient(port);
        professionalUserRepository.deleteAll();
        organisationRepository.deleteAll();
        
        Organisation organisation = new Organisation("some-org-name", "PENDING");
        ProfessionalUser superUser = new ProfessionalUser("some-fname", "some-lname", "someone@somewhere.com", "PENDING", organisation);
        organisationRepository.save(organisation);
        professionalUserRepository.save(superUser);
    }

    @Test
    public void search_returns_valid_user() {

        Map<String, Object> response =
                professionalReferenceDataClient.findUserByEmail("someone@somewhere.com");

        assertEquals("some-fname", response.get("firstName"));
        assertEquals("some-lname", response.get("lastName"));
        assertEquals("someone@somewhere.com", response.get("email"));
    }

    @Test
    public void returns_404_when_email_not_found() {

        Map<String, Object> response =
                professionalReferenceDataClient.findUserByEmail("someone@nowhere.com");

        assertThat(response.get("http_status")).isEqualTo("404");
    }
}
