package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createJurisdictions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;

import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;


@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
public class CreateOrganisationWithContactInformationDxAddress extends AuthorizationEnabledIntegrationTest {

    @Test
    public void persists_and_returns_valid_organisation_with_contact_and_dxAddress() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id")
                .sraRegulated("false")
                .companyUrl("company-url")
                .companyNumber(randomAlphabetic(8))
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .jurisdictions(createJurisdictions())
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
                .findByOrganisationIdentifier(orgIdentifierResponse);
        assertThat(persistedOrganisation.getOrganisationIdentifier().toString()).isEqualTo(orgIdentifierResponse);
        assertThat(persistedOrganisation.getContactInformation().size()).isEqualTo(1);

    }

    @Test
    public void persists_and_returns_400_organisation_with_invalid_length_of_company_number() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id-number")
                .sraRegulated("false")
                .companyUrl("company-url")
                .companyNumber(randomAlphabetic(9))
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
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    public void returns_bad_request_when_dx_num_invalid() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id-number")
                .sraRegulated("false")
                .companyUrl("company-url")
                .companyNumber("companyno")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .jurisdictions(createJurisdictions())
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("this is an invalid dx number")
                                .dxExchange("dxExchange").build()))
                        .build()))
                .build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    public void returns_bad_request_when_address_line1_empty() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id-number")
                .sraRegulated("false")
                .companyUrl("company-url")
                .companyNumber("companyno")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX 1234567890")
                                .dxExchange("dxExchange").build()))
                        .build()))
                .build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        assertThat(response.get("http_status")).isEqualTo("400");

    }

    @Test
    public void persists_and_returns_201_organisation_for_same_sra_id_and_same_sra_url() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id-number")
                .sraRegulated("false")
                .companyUrl("company-url")
                .companyNumber(randomAlphabetic(8))
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .jurisdictions(createJurisdictions())
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .build()))
                .build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        OrganisationCreationRequest organisationCreationRequest2 = anOrganisationCreationRequest()
                .name("some-org-name1")
                .sraId("sra-id-number1")
                .sraRegulated("false")
                .companyUrl("company-url")
                .companyNumber(randomAlphabetic(8))
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone1@somewhere.com")
                        .jurisdictions(createJurisdictions())
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine2")
                        .build()))
                .build();
        Map<String, Object> response2 =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest2);
        assertThat(response2.get("http_status")).isEqualTo("201 CREATED");
    }


    @Test
    public void persists_and_returns_valid_organisation_with_contact_and_dxAddress_null() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id")
                .sraRegulated("false")
                .companyUrl("company-url")
                .companyNumber(randomAlphabetic(8))
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .jurisdictions(createJurisdictions())
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .build()))
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(orgIdentifierResponse);
        assertThat(persistedOrganisation.getOrganisationIdentifier()).isEqualTo(orgIdentifierResponse);
        assertThat(persistedOrganisation.getContactInformation().size()).isEqualTo(1);
        assertThat(persistedOrganisation.getContactInformation().get(0).getDxAddresses().size()).isEqualTo(0);
    }

    @Test
    public void returns_bad_request_when_dxnumber_is_null() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id")
                .sraRegulated("false")
                .companyUrl("company-url")
                .companyNumber(randomAlphabetic(8))
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .jurisdictions(createJurisdictions())
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
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName(null)
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .jurisdictions(createJurisdictions())
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
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .companyNumber(randomAlphabetic(8))
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("firstname")
                        .lastName(null)
                        .email("someone@somewhere.com")
                        .jurisdictions(createJurisdictions())
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
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("firstname")
                        .lastName("some-lname")
                        .email(null)
                        .jurisdictions(createJurisdictions())
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
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("firstname")
                        .lastName("some-lname")
                        .email("some@email.com")
                        .jurisdictions(createJurisdictions())
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

    @Test
    public void persists_organisation_with_white_spaces_removed() {
        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name(" some-org-name ")
                .sraId(" sra-id ")
                .sraRegulated("false")
                .companyUrl(" company-url ")
                .companyNumber("companyn")
                .superUser(aUserCreationRequest()
                        .firstName(" some-fname ")
                        .lastName(" some-lname ")
                        .email(" someone@somewhere.com ")
                        .jurisdictions(createJurisdictions())
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1(" addressLine1 ")
                        .addressLine2(" ad  2 ").addressLine3(" ad3 ")
                        .country(" Ireland ")
                        .county(" Laois ")
                        .postCode(" W127AE ")
                        .townCity(" Dublin ")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX 1234567890")
                                .dxExchange("dxExchange").build()))
                        .build()))
                .build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(orgIdentifierResponse);
        assertThat(persistedOrganisation.getOrganisationIdentifier().toString()).isEqualTo(orgIdentifierResponse);
        assertThat(persistedOrganisation.getContactInformation().size()).isEqualTo(1);
        assertThat(persistedOrganisation.getName()).isEqualTo("some-org-name");
        assertThat(persistedOrganisation.getSraId()).isEqualTo("sra-id");
        assertThat(persistedOrganisation.getCompanyUrl()).isEqualTo("company-url");
        assertThat(persistedOrganisation.getCompanyNumber()).isEqualTo("companyn");

        SuperUser persistedSuperUser = persistedOrganisation.getUsers().get(0);

        assertThat(persistedSuperUser.getFirstName()).isEqualTo("some-fname");
        assertThat(persistedSuperUser.getLastName()).isEqualTo("some-lname");
        assertThat(persistedSuperUser.getEmailAddress()).isEqualTo("someone@somewhere.com");

        List<ContactInformation> contactInformation = persistedOrganisation.getContactInformations();

        assertThat(contactInformation.get(0).getAddressLine1()).isEqualTo("addressLine1");
        assertThat(contactInformation.get(0).getAddressLine2()).isEqualTo("ad 2");
        assertThat(contactInformation.get(0).getAddressLine3()).isEqualTo("ad3");
        assertThat(contactInformation.get(0).getCountry()).isEqualTo("Ireland");
        assertThat(contactInformation.get(0).getCounty()).isEqualTo("Laois");
        assertThat(contactInformation.get(0).getPostCode()).isEqualTo("W127AE");
        assertThat(contactInformation.get(0).getTownCity()).isEqualTo("Dublin");
    }

    @Test
    public void persists_organisation_with_sraRegulated_true_case_insensitive() {
        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id")
                .sraRegulated("TrUe")
                .companyUrl("company-url")
                .companyNumber(randomAlphabetic(8))
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .jurisdictions(createJurisdictions())
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
                .findByOrganisationIdentifier(orgIdentifierResponse);
        assertThat(persistedOrganisation.getOrganisationIdentifier().toString()).isEqualTo(orgIdentifierResponse);
        assertThat(persistedOrganisation.getSraRegulated()).isTrue();
    }

    @Test
    public void persists_organisation_with_sraRegulated_false_case_insensitive() {
        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id")
                .sraRegulated("FaLsE")
                .companyUrl("company-url")
                .companyNumber(randomAlphabetic(8))
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .jurisdictions(createJurisdictions())
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
                .findByOrganisationIdentifier(orgIdentifierResponse);
        assertThat(persistedOrganisation.getOrganisationIdentifier().toString()).isEqualTo(orgIdentifierResponse);
        assertThat(persistedOrganisation.getSraRegulated()).isFalse();
    }
}