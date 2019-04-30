package uk.gov.hmcts.reform.professionalapi;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.professionalapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.*;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.ProfessionalReferenceDataClient;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.DXAddressCreationRequest.dxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.ContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.PbaAccountCreationRequest.aPbaPaymentAccount;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateOrganisationWithContactInformationDxAddress extends Service2ServiceEnabledIntegrationTest {

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private ProfessionalUserRepository professionalUserRepository;

    @Autowired
    private PaymentAccountRepository paymentAccountRepository;

    @Autowired
    private ContactInformationRepository contactInformationRepository;

    @Autowired
    private DXAddressRepository dxAddressRepository;

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
    public void persists_and_returns_valid_organisation_with_contact_and_dxaddres() {

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
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX 1234567890")
                                .dxExchange("dxExchange").build()))
                        .build()))
                .build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(UUID.fromString(orgIdentifierResponse));
        assertThat(persistedOrganisation.getOrganisationIdentifier().toString()).isEqualTo(orgIdentifierResponse);
        assertThat(persistedOrganisation.getContactInformation().size()).isEqualTo(1);
        assertThat(persistedOrganisation.getContactInformation().get(0).getDxAddresses().size()).isEqualTo(1);
    }

    @Test
    public void returns_bad_request_when_user_first_name_null() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .pbaAccounts(asList(aPbaPaymentAccount()
                        .pbaNumber("pbaNumber-1")
                        .build()))
                .superUser(aUserCreationRequest()
                        .firstName(null)
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .build()))
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(paymentAccountRepository.findAll().size()).isEqualTo(0);
        assertThat(organisationRepository.findAll().size()).isEqualTo(0);
        assertThat(professionalUserRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    public void returns_bad_request_when_user_LastName_null() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .pbaAccounts(asList(aPbaPaymentAccount()
                        .pbaNumber("pbaNumber-1")
                        .build()))
                .superUser(aUserCreationRequest()
                        .firstName("firstname")
                        .lastName(null)
                        .email("someone@somewhere.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .build()))
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(paymentAccountRepository.findAll().size()).isEqualTo(0);
        assertThat(organisationRepository.findAll().size()).isEqualTo(0);
        assertThat(professionalUserRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    public void returns_bad_request_when_user_email_null() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .pbaAccounts(asList(aPbaPaymentAccount()
                        .pbaNumber("pbaNumber-1")
                        .build()))
                .superUser(aUserCreationRequest()
                        .firstName("firstname")
                        .lastName("some-lname")
                        .email(null)
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .build()))
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(paymentAccountRepository.findAll().size()).isEqualTo(0);
        assertThat(organisationRepository.findAll().size()).isEqualTo(0);
        assertThat(professionalUserRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    public void returns_bad_request_when_contact_information_null() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .pbaAccounts(asList(aPbaPaymentAccount()
                        .pbaNumber("pbaNumber-1")
                        .build()))
                .superUser(aUserCreationRequest()
                        .firstName("firstname")
                        .lastName("some-lname")
                        .email(null)
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .build()))
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(paymentAccountRepository.findAll().size()).isEqualTo(0);
        assertThat(organisationRepository.findAll().size()).isEqualTo(0);
        assertThat(professionalUserRepository.findAll().size()).isEqualTo(0);
    }
}
