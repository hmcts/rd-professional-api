package uk.gov.hmcts.reform.professionalapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.getContactInformationList;

public class AddContactInformationToOrganisationTest extends AuthorizationEnabledIntegrationTest {

    OrganisationCreationRequest organisationCreationRequest = null;
    Map<String, Object> orgResponse = null;
    String userId = null;
    List<ContactInformationCreationRequest> contactInformationCreationRequests = null;
    String orgId = null;

    @BeforeEach
    public void setUpOrganisationData() {

         organisationCreationRequest = organisationRequestWithAllFields()
                .build();
        orgResponse = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

         orgId = (String) orgResponse.get(ORG_IDENTIFIER);

         userId = updateOrgAndInviteUser(orgId, puiOrgManager);
         contactInformationCreationRequests = getContactInformationList();

    }

    @Test
    void add_contact_informations_to_organisation() {

        Map<String, Object> addContactsToOrgresponse =
                professionalReferenceDataClient.addContactInformationsToOrganisation(contactInformationCreationRequests,orgId,puiOrgManager,userId);

        assertThat(addContactsToOrgresponse).isNotNull();
        assertThat(addContactsToOrgresponse.get("http_status")).isEqualTo("201 CREATED");
    }

    @Test
    void add_contact_informations_to_organisation_returns_403_when_forbidden_user_role() {

        Map<String, Object> addContactsToOrgresponse =
                professionalReferenceDataClient.addContactInformationsToOrganisation(contactInformationCreationRequests,orgId,puiFinanceManager,userId);

        assertThat(addContactsToOrgresponse).isNotNull();
        ErrorResponse errorResponse = getErrorResponse(addContactsToOrgresponse.get("response_body").toString());

        assertThat(errorResponse.getErrorDescription()).contains("Access is denied");
        assertThat(addContactsToOrgresponse.get("http_status")).isEqualTo("403");
    }

    @Test
    void add_contact_informations_to_organisation_returns_401_when_unauthorised_user() {

        Map<String, Object> addContactsToOrgresponse =
                professionalReferenceDataClient.addContactInformationsToOrganisation(contactInformationCreationRequests,orgId,null,null);

        assertThat(addContactsToOrgresponse).isNotNull();
        ErrorResponse errorResponse = getErrorResponse(addContactsToOrgresponse.get("response_body").toString());

        assertThat(addContactsToOrgresponse.get("http_status")).isEqualTo("401");
    }

    @Test
    void add_contact_informations_to_organisation_returns_404_when_orgId_is_missing() {

        orgId = null;
        Map<String, Object> addContactsToOrgresponse =
                professionalReferenceDataClient.addContactInformationsToOrganisation(contactInformationCreationRequests,orgId,puiOrgManager,userId);

        assertThat(addContactsToOrgresponse).isNotNull();
        ErrorResponse errorResponse = getErrorResponse(addContactsToOrgresponse.get("response_body").toString());

        assertThat(errorResponse.getErrorMessage()).contains("Resource not found");
        assertThat(addContactsToOrgresponse.get("http_status")).isEqualTo("404");
    }

    @Test
    void add_contact_informations_to_organisation_returns_404_when_empty_contact_information_list() {

        contactInformationCreationRequests = new ArrayList<>();
        Map<String, Object> addContactsToOrgresponse =
                professionalReferenceDataClient.addContactInformationsToOrganisation(contactInformationCreationRequests,orgId,puiOrgManager,userId);

        assertThat(addContactsToOrgresponse).isNotNull();
        ErrorResponse errorResponse = getErrorResponse(addContactsToOrgresponse.get("response_body").toString());

        assertThat(errorResponse.getErrorMessage()).contains("Resource not found");
        assertThat(errorResponse.getErrorDescription()).contains("Request is empty");
        assertThat(addContactsToOrgresponse.get("http_status")).isEqualTo("404");
    }

    @Test
    void add_contact_informations_to_organisation_returns_400_when_contact_information_addressLine1_is_missing() {
        contactInformationCreationRequests = Arrays.asList(aContactInformationCreationRequest()
                //.addressLine1("addressLine1")
                .addressLine2("addressLine2")
                .addressLine3("addressLine3")
                .country("country")
                .county("county")
                .townCity("town-city")
                .uprn("uprn")
                .postCode("some-post-code")
                .dxAddress(Arrays.asList(dxAddressCreationRequest()
                        .dxNumber("DX 1234567890")
                        .dxExchange("dxExchange").build()))
                .build());

        Map<String, Object> addContactsToOrgresponse =
                professionalReferenceDataClient.addContactInformationsToOrganisation(contactInformationCreationRequests,orgId,puiOrgManager,userId);

        assertThat(addContactsToOrgresponse).isNotNull();
        ErrorResponse errorResponse = getErrorResponse(addContactsToOrgresponse.get("response_body").toString());

        assertThat(errorResponse.getErrorMessage()).contains("Resource not found");
        assertThat(addContactsToOrgresponse.get("http_status")).isEqualTo("404");
    }

    @Test
    void persists_and_returns_400_organisation_with_invalid_length_of_company_number() {

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields()
                .companyNumber(randomAlphabetic(9))
                .build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    void returns_bad_request_when_dx_num_invalid() {

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields()
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
    void can_create_an_organisation_with_Dx_Number_less_than_13() {

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields()
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX123456")
                                .dxExchange("dxExchange").build()))
                        .build()))
                .build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        assertThat(response.get("http_status")).isEqualTo("201 CREATED");
    }

    @Test
    void create_an_organisation_with_Dx_Number_more_than_13_throws_400() {

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields()
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX1234567891011")
                                .dxExchange("dxExchange").build()))
                        .build()))
                .build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    void create_an_organisation_with_Dx_Exchange_more_than_20_throws_400() {

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields()
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX123456789")
                                .dxExchange("dxExchange12345678901").build()))
                        .build()))
                .build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    void create_an_organisation_with_Dx_Number_empty_returns_400() {

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields()
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("")
                                .dxExchange("dxExchange").build()))
                        .build()))
                .build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    void create_an_organisation_with_Dx_Exchange_empty_returns_400() {

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields()
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX123456789")
                                .dxExchange("").build()))
                        .build()))
                .build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    void create_an_organisation_with_Dx_Number_null_returns_400() {

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields()
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber(null)
                                .dxExchange("dxExchange").build()))
                        .build()))
                .build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    void create_an_organisation_with_Dx_Exchange_null_returns_400() {

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields()
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber(null)
                                .dxExchange("").build()))
                        .build()))
                .build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    void returns_bad_request_when_address_line1_empty() {

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields()
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
    void persists_and_returns_201_organisation_for_same_sra_id_and_same_sra_url() {

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
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine2")
                        .build()))
                .build();
        Map<String, Object> response2 =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest2);
        assertThat(response2.get("http_status")).isEqualTo("201 CREATED");
    }


    @Test
    void persists_and_returns_valid_organisation_with_contact_and_dxAddress_null() {

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
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .build()))
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get(ORG_IDENTIFIER);
        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(orgIdentifierResponse);
        assertThat(persistedOrganisation.getOrganisationIdentifier()).isEqualTo(orgIdentifierResponse);
        assertThat(persistedOrganisation.getContactInformation().size()).isEqualTo(1);
        assertThat(persistedOrganisation.getContactInformation().get(0).getDxAddresses().size()).isEqualTo(0);
    }

    @Test
    void returns_bad_request_when_dxnumber_is_null() {

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields()
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
    void returns_bad_request_when_user_first_name_null() {
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccount(paymentAccounts)
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
    void returns_bad_request_when_user_LastName_null() {
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
    void returns_bad_request_when_user_email_null() {
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccount(paymentAccounts)
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
    void returns_bad_request_when_contact_information_null() {
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("firstname")
                        .lastName("some-lname")
                        .email("some@email.com")
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
    void persists_organisation_with_white_spaces_removed() {
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
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().uprn("uprn")
                        .addressLine1(" addressLine1 ")
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

        String orgIdentifierResponse = (String) response.get(ORG_IDENTIFIER);
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

        assertThat(contactInformation.get(0).getUprn()).isEqualTo("uprn");
        assertThat(contactInformation.get(0).getAddressLine1()).isEqualTo("addressLine1");
        assertThat(contactInformation.get(0).getAddressLine2()).isEqualTo("ad 2");
        assertThat(contactInformation.get(0).getAddressLine3()).isEqualTo("ad3");
        assertThat(contactInformation.get(0).getCountry()).isEqualTo("Ireland");
        assertThat(contactInformation.get(0).getCounty()).isEqualTo("Laois");
        assertThat(contactInformation.get(0).getPostCode()).isEqualTo("W127AE");
        assertThat(contactInformation.get(0).getTownCity()).isEqualTo("Dublin");
    }

    @Test
    void persists_organisation_with_sraRegulated_true_case_insensitive() {
        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields()
                .sraRegulated("TrUe")
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX 1234567890")
                                .dxExchange("dxExchange").build()))
                        .build()))
                .build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get(ORG_IDENTIFIER);
        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(orgIdentifierResponse);
        assertThat(persistedOrganisation.getOrganisationIdentifier().toString()).isEqualTo(orgIdentifierResponse);
        assertThat(persistedOrganisation.getSraRegulated()).isTrue();
    }

    @Test
    void persists_organisation_with_sraRegulated_false_case_insensitive() {
        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields()
                .sraRegulated("FaLsE")
                .build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get(ORG_IDENTIFIER);
        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(orgIdentifierResponse);
        assertThat(persistedOrganisation.getOrganisationIdentifier().toString()).isEqualTo(orgIdentifierResponse);
        assertThat(persistedOrganisation.getSraRegulated()).isFalse();
    }

    @Test
    void returns_bad_request_when_dxnumber_or_dx_exchange_is_empty() {

        //dx_number are empty
        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields()
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber(" ")
                                .dxExchange("dxExchange").build()))
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

        //dx_exchange are empty
        OrganisationCreationRequest organisationCreationRequest1 = organisationRequestWithAllFields()
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX 123456789")
                                .dxExchange(" ").build()))
                        .build()))
                .build();


        Map<String, Object> response1 =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest1);

        assertThat(response1.get("http_status")).isEqualTo("400");

        //dx_exchange and dx_address are empty
        OrganisationCreationRequest organisationCreationRequest2 = organisationRequestWithAllFields()
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber(" ")
                                .dxExchange(" ").build()))
                        .build()))
                .build();


        Map<String, Object> response2 =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest2);
        assertThat(response2.get("http_status")).isEqualTo("400");
    }

    private String createActiveUserAndOrganisation(String orgIdentifierResponse , boolean isActive) {

        String userId = updateOrgAndInviteUser(orgIdentifierResponse, puiOrgManager);

       // java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
        //        .createOrganisation(organisationCreationRequest);
        //String orgId = (String) responseForOrganisationCreation.get(ORG_IDENTIFIER);
       // if (isActive) {
         //   updateOrganisation(orgId, hmctsAdmin, ACTIVE);
       // }
        return userId;
    }

    private ErrorResponse getErrorResponse(String errorDetails){
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        ErrorResponse errorResponse=null;
        try {
            errorResponse =  mapper.readValue(errorDetails,ErrorResponse.class);
        } catch (JsonProcessingException e) {
            errorResponse = null;
        }
        return errorResponse;
    }

}