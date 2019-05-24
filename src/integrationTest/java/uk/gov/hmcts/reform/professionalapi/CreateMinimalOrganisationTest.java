package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;


public class CreateMinimalOrganisationTest extends Service2ServiceEnabledIntegrationTest {

    @Test
    public void persists_and_returns_valid_minimal_organisation() {
        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");

        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(UUID.fromString(orgIdentifierResponse));

        ProfessionalUser persistedSuperUser = persistedOrganisation.getUsers().get(0);

        assertThat(persistedOrganisation.getOrganisationIdentifier().toString()).isEqualTo(orgIdentifierResponse);
        assertThat(persistedOrganisation.getStatus()).isEqualTo(OrganisationStatus.PENDING);
        assertThat(persistedOrganisation.getUsers().size()).isEqualTo(1);

        assertThat(persistedSuperUser.getUserIdentifier()).isNotNull();
        assertThat(persistedSuperUser.getUserIdentifier().toString().length()).isEqualTo(36);
        assertThat(persistedSuperUser.getEmailAddress()).isEqualTo("someone@somewhere.com");
        assertThat(persistedSuperUser.getFirstName()).isEqualTo("some-fname");
        assertThat(persistedSuperUser.getLastName()).isEqualTo("some-lname");
        assertThat(persistedSuperUser.getStatus()).isEqualTo(ProfessionalUserStatus.PENDING);
        assertThat(persistedSuperUser.getOrganisation().getName()).isEqualTo("some-org-name");
        assertThat(persistedSuperUser.getOrganisation().getId()).isEqualTo(persistedOrganisation.getId());

        assertThat(persistedOrganisation.getName()).isEqualTo("some-org-name");

    }

    @Test
    public void returns_400_when_mandatory_data_not_present() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name(null)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .build())
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body").toString().contains("Bad Request"));

        assertThat(organisationRepository.findAll()).isEmpty();
    }

    @Test
    public void returns_500_when_database_constraint_violated() {

        String organisationNameViolatingDatabaseMaxLengthConstraint = RandomStringUtils.random(256);

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest()
                .name(organisationNameViolatingDatabaseMaxLengthConstraint).build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        assertThat(response.get("http_status")).isEqualTo("500");
    }
}
