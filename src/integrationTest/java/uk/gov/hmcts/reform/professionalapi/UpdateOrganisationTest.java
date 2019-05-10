package uk.gov.hmcts.reform.professionalapi;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.persistence.*;
import uk.gov.hmcts.reform.professionalapi.util.ProfessionalReferenceDataClient;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;



public class UpdateOrganisationTest extends Service2ServiceEnabledIntegrationTest {

	@Autowired
	private OrganisationRepository organisationRepository;

	@Autowired
	private ProfessionalUserRepository professionalUserRepository;

	@Autowired
	private ContactInformationRepository contactInformationRepository;

	@Autowired
	private DxAddressRepository dxAddressRepository;

	@Autowired
	private PaymentAccountRepository paymentAccountRepository;

	private ProfessionalReferenceDataClient professionalReferenceDataClient;

	@Before
	public void setUp() {
		professionalReferenceDataClient = new ProfessionalReferenceDataClient(port);
		dxAddressRepository.deleteAll();
		contactInformationRepository.deleteAll();
		professionalUserRepository.deleteAll();
		paymentAccountRepository.deleteAll();
		organisationRepository.deleteAll();
	}

	@Test
	public void updates_organisation_and_returns_status_200() {

		//String organisationIdentifier = "9a9b1a14-00d1-460a-badc-e98137a6dc1f";
		//1. Create Organisation
		OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
				.name("some-org-name")
				.sraId("sra-id")
				.sraRegulated(Boolean.FALSE)
				.companyUrl("company-url")
				.companyNumber("companyn")
				.superUser(aUserCreationRequest()
						.firstName("some-fname")
						.lastName("some-lname")
						.email("someone@somewhere.com")
						.build())
				.contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1").build()))
				.build();

		Map<String, Object> responseForOrganisationCreation =
				professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

		String organisationIdentifier = (String) responseForOrganisationCreation.get("organisationIdentifier");

		Organisation persistedOrganisation = organisationRepository
				.findByOrganisationIdentifier(UUID.fromString(organisationIdentifier));

		assertThat(persistedOrganisation.getStatus()).isEqualTo(OrganisationStatus.PENDING);

		//2. Updating organisation status to active
		organisationCreationRequest = anOrganisationCreationRequest()
				.name("some-org-name1")
				.status(OrganisationStatus.ACTIVE)
				.sraId("sra-id1")
				.sraRegulated(Boolean.TRUE)
				.companyUrl("company-url1")
				.companyNumber("company1")
				.superUser(aUserCreationRequest()
						.firstName("some-fname")
						.lastName("some-lname")
						.email("someone@somewhere.com")
						.build())
				.contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1").build()))
				.build();
		Map<String, Object> responseForOrganisationUpdate =
				professionalReferenceDataClient.updateOrganisation(organisationCreationRequest, organisationIdentifier);

		persistedOrganisation = organisationRepository
				.findByOrganisationIdentifier(UUID.fromString(organisationIdentifier));

		assertThat(persistedOrganisation.getName()).isEqualTo("some-org-name1");
		assertThat(persistedOrganisation.getStatus()).isEqualTo(OrganisationStatus.ACTIVE);
		assertThat(persistedOrganisation.getSraId()).isEqualTo("sra-id1");
		assertThat(persistedOrganisation.getSraRegulated()).isEqualTo(Boolean.TRUE);
		assertThat(persistedOrganisation.getCompanyUrl()).isEqualTo("company-url1");

		assertThat(responseForOrganisationUpdate.get("http_status")).isEqualTo(200);
	}

	@Test
	public void updates_non_existing_organisation_returns_status_404() {

		OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
				.name("some-org-name")
				.status(OrganisationStatus.ACTIVE)
				.sraId("sra-id")
				.sraRegulated(Boolean.TRUE)
				.companyUrl("company-url1")
				.companyNumber("company1")
				.superUser(aUserCreationRequest()
						.firstName("some-fname")
						.lastName("some-lname")
						.email("someone@somewhere.com")
						.build())
				.contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1").build()))
				.build();
		Map<String, Object> responseForOrganisationUpdate =
				professionalReferenceDataClient.updateOrganisation(organisationCreationRequest, UUID.randomUUID().toString());

		assertThat(responseForOrganisationUpdate.get("http_status")).isEqualTo("404");
	}

	@Test
	public void updates_organisation_with_organisation_identifier_null_returns_status_400() {

		OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
				.name("some-org-name")
				.status(OrganisationStatus.ACTIVE)
				.sraId("sra-id")
				.sraRegulated(Boolean.TRUE)
				.companyUrl("company-url1")
				.companyNumber("company1")
				.superUser(aUserCreationRequest()
						.firstName("some-fname")
						.lastName("some-lname")
						.email("someone@somewhere.com")
						.build())
				.contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1").build()))
				.build();
		Map<String, Object> responseForOrganisationUpdate =
				professionalReferenceDataClient.updateOrganisation(organisationCreationRequest, null);

		assertThat(responseForOrganisationUpdate.get("http_status")).isEqualTo("400");
	}

	@Test
	public void updates_organisation_with_invalid_organisation_identifier_returns_status_400() {

		OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
				.name("some-org-name")
				.status(OrganisationStatus.ACTIVE)
				.sraId("sra-id")
				.sraRegulated(Boolean.TRUE)
				.companyUrl("company-url1")
				.companyNumber("company1")
				.superUser(aUserCreationRequest()
						.firstName("some-fname")
						.lastName("some-lname")
						.email("someone@somewhere.com")
						.build())
				.contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1").build()))
				.build();
		Map<String, Object> responseForOrganisationUpdate =
				professionalReferenceDataClient.updateOrganisation(organisationCreationRequest, "1234ab12");

		assertThat(responseForOrganisationUpdate.get("http_status")).isEqualTo("400");
	}
}
