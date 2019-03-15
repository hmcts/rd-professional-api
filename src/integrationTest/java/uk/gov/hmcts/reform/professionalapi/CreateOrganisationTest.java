package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.professionalapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.entities.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.util.ProfessionalReferenceDataClient;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;

public class CreateOrganisationTest  extends Service2ServiceEnabledIntegrationTest {

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private ProfessionalUserRepository professionalUserRepository;

    private ProfessionalReferenceDataClient professionalReferenceDataClient;

    @Transactional
    @Before
    public void setUp() {
        professionalReferenceDataClient = new ProfessionalReferenceDataClient(port);
        professionalUserRepository.deleteAll();
        organisationRepository.deleteAll();
    }

    @Transactional
    @Test
    public void persists_and_returns_valid_minimal_organisation() {

        Map<String, Object> response = professionalReferenceDataClient.createOrganisation(
                "some-org-name",
                "some-fname",
                "some-lname",
                "someone@somewhere.com"
        );

        String nameFromResponse = (String) response.get("name");

        Organisation persistedOrganisation = organisationRepository
                .findByName((String) response.get("name"));

        ProfessionalUser persistedSuperUser = persistedOrganisation.getUsers().get(0);

        assertThat(persistedOrganisation.getName()).isEqualTo(nameFromResponse);
        assertThat(persistedOrganisation.getStatus()).isEqualTo("PENDING");
        assertThat(persistedOrganisation.getUsers().size()).isEqualTo(1);

        assertThat(persistedSuperUser.getEmailAddress()).isEqualTo("someone@somewhere.com");
        assertThat(persistedSuperUser.getFirstName()).isEqualTo("some-fname");
        assertThat(persistedSuperUser.getLastName()).isEqualTo("some-lname");
        assertThat(persistedSuperUser.getStatus()).isEqualTo("PENDING");
        assertThat(persistedSuperUser.getOrganisation().getName()).isEqualTo(nameFromResponse);

        assertThat(nameFromResponse).isEqualTo("some-org-name");
        assertThat((List<String>)response.get("userIds"))
                .containsExactly(persistedSuperUser.getId().toString());
    }

    @Transactional
    @Test
    public void returns_400_when_mandatory_data_not_present() {

        Map<String, Object> response = professionalReferenceDataClient.createOrganisation(
                null,
                "some-fname",
                "some-lname",
                "someone@somewhere.com"
        );

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).isEqualTo("");

        assertThat(organisationRepository.findAll()).isEmpty();
    }

    @Transactional
    @Test
    public void returns_400_when_database_constraint_violated() {

        String organisationNameViolatingDatabaseMaxLengthConstraint = RandomStringUtils.random(256);

        Map<String, Object> response = professionalReferenceDataClient.createOrganisation(
                organisationNameViolatingDatabaseMaxLengthConstraint,
                "some-fname",
                "some-lname",
                "someone@somewhere.com"
        );

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).isEqualTo("");

        assertThat(organisationRepository.findAll()).isEmpty();
    }
}
