package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;



public class CreateOrganisationWithContactInformationDxAddress extends Service2ServiceEnabledIntegrationTest {

    @Test
    public void persists_and_returns_valid_organisation_with_contact_and_dxAddress() {

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

    }

    @Test
    public void persists_and_returns_500_organisation_with_invalid_length_of_company_number() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id-number")
                .sraRegulated(Boolean.FALSE)
                .companyUrl("company-url")
                .companyNumber("companyno")
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
        assertThat(response.get("http_status")).isEqualTo("500");
    }

    @Test
    public void persists_and_returns_400_organisation_with_unique_constraint_violated_sra_id_and_dxAddress() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id-number")
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

        OrganisationCreationRequest organisationCreationRequest2 = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id-number")
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
        Map<String, Object> response2 =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        assertThat(response2.get("http_status")).isEqualTo("400");
    }

    @Test
    public void persists_and_returns_400_organisation_with_unique_constraint_violated_for_company_url() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id-number")
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

        OrganisationCreationRequest organisationCreationRequest2 = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id-number1")
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
        Map<String, Object> response2 =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        assertThat(response2.get("http_status")).isEqualTo("400");
    }

    @Test
    public void persists_and_returns_valid_organisation_with_contact_and_dxAddress_null() {

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
                                                  .build()))
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(UUID.fromString(orgIdentifierResponse));
        assertThat(persistedOrganisation.getOrganisationIdentifier().toString()).isEqualTo(orgIdentifierResponse);
        assertThat(persistedOrganisation.getContactInformation().size()).isEqualTo(1);
        assertThat(persistedOrganisation.getContactInformation().get(0).getDxAddresses().size()).isEqualTo(0);
    }

    @Test
    public void returns_bad_request_when_dxnumber_is_null() {

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
                                                                           .dxNumber(null)
                                                                           .dxExchange(null).build()))
                                                  .build()))
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(paymentAccountRepository.findAll().size()).isEqualTo(0);
        assertThat(organisationRepository.findAll().size()).isEqualTo(0);
        assertThat(professionalUserRepository.findAll().size()).isEqualTo(0);
        assertThat(contactInformationRepository.findAll().size()).isEqualTo(0);
        assertThat(dxAddressRepository.findAll().size()).isEqualTo(0);
    }

    @Test
    public void returns_bad_request_when_user_first_name_null() {
        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("pba123");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccounts(paymentAccounts)
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
        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("pba123");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccounts(paymentAccounts)
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
        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("pba123");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccounts(paymentAccounts)
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
        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("pba123");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccounts(paymentAccounts)
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