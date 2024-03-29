package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.configuration.ApplicationConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrgAttributeRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.service.impl.PaymentAccountServiceImpl;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_NO_ORGANISATION_FOUND;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_NO_PBA_FOUND;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;


class FindPaymentAccountsByEmailTest extends AuthorizationEnabledIntegrationTest {

    @Autowired
    PaymentAccountServiceImpl paymentAccountService;

    @Autowired
    ApplicationConfiguration configuration;

    @Test
    void get_request_returns_correct_payment_accounts_associated_with_email() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("some@email.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1").build()))
                .build();

        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String uuid = (String)organisationResponse.get(ORG_IDENTIFIER);

        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
                .status("ACTIVE").build();

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, hmctsAdmin, uuid);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .findPaymentAccountsByEmailFromHeader("some@email.com", hmctsAdmin);

        assertThat(orgResponse).isNotEmpty();
        responseValidate(orgResponse);

    }

    @Test
    void returns_multiple_correct_payment_accounts_associated_with_email() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");
        paymentAccounts.add("PBA1234568");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("some@email.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .build()))
                .build();

        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String uuid = (String)organisationResponse.get(ORG_IDENTIFIER);

        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
                .status("ACTIVE").build();

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, hmctsAdmin, uuid);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .findPaymentAccountsByEmailFromHeader("some@email.com", hmctsAdmin);

        assertThat(orgResponse).isNotEmpty();

        responseValidate(orgResponse);
    }

    @Test
    void returns_404_organisation_user_accounts_associated_with_email_and_no_payment_account() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("some@email.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .build()))
                .build();

        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String uuid = (String)organisationResponse.get(ORG_IDENTIFIER);

        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
                .status("ACTIVE").build();

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, hmctsAdmin, uuid);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .findPaymentAccountsByEmailFromHeader("some@email.com", hmctsAdmin);
        assertThat(orgResponse.get("http_status")).isEqualTo("404");

    }

    @Test
    void return_404_when_organisation_status_pending_for_pba_user_email_address() {

        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-")
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("some@email.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .build()))
                .build();

        professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        Map<String, Object> response = professionalReferenceDataClient
                .findPaymentAccountsByEmailFromHeader("some@email.com", hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("404");

    }

    @Test
    void returns_404_when_email_not_found() {

        Map<String, Object> response = professionalReferenceDataClient
                .findPaymentAccountsByEmailFromHeader("wrong@email.com", hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("404");
    }

    @Test
    void get_request_returns_correct_payment_accounts_associated_with_email_fromHeader_internal() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("some@email.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1").build()))
                .build();

        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String uuid = (String)organisationResponse.get(ORG_IDENTIFIER);

        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
                .status("ACTIVE").build();

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, hmctsAdmin, uuid);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .findPaymentAccountsByEmailFromHeader("some@email.com", hmctsAdmin);

        assertThat(orgResponse).isNotEmpty();
        responseValidate(orgResponse);

    }

    @Test
    void returns_404_when_unkown_email_not_found_fromDb() {

        Map<String, Object> response =
                professionalReferenceDataClient.findPaymentAccountsByEmailFromHeader("wrong@email.com", hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("404");
    }

    @Test
    void returns_400_whenEmailIsNullInBothHeaderAndParam() {

        Map<String, Object> response =
                professionalReferenceDataClient.findPaymentAccountsByEmailFromHeader("", hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("400");
    }


    @Test
    void get_request_returns_correct_payment_accounts_associated_with_email_fromHeader_external() {
        String userId = settingUpOrganisation(puiUserManager);
        Map<String, Object> orgResponse = professionalReferenceDataClient
                .findPaymentAccountsByEmailFromHeaderForExternalUsers("someone@somewhere.com", puiUserManager, userId);
        assertThat(orgResponse).isNotEmpty();
        responseValidate(orgResponse);

    }

    @Test
    void get_request_returns_404_when_unknown_email_passed_in_header_external() {
        String userId = settingUpOrganisation(puiUserManager);
        Map<String, Object> orgResponse = professionalReferenceDataClient
                .findPaymentAccountsByEmailFromHeaderForExternalUsers("dummy23@dummy.com", puiUserManager, userId);
        assertThat(orgResponse.get("http_status")).isEqualTo("404");
        assertThat((String)orgResponse.get("response_body")).contains(ERROR_MSG_NO_ORGANISATION_FOUND);
    }

    @Test
    void get_request_returns_404_when_organisation_doesnt_have_pba_email_passed_in_header_external() {
        Pair<String,String> userInfo = settingUpMinimalFieldOrganisation(puiUserManager);
        Map<String, Object> orgResponse = professionalReferenceDataClient
                .findPaymentAccountsByEmailFromHeaderForExternalUsers(userInfo.getFirst(), puiUserManager,
                        userInfo.getSecond());
        assertThat(orgResponse.get("http_status")).isEqualTo("404");
        assertThat((String)orgResponse.get("response_body")).contains(ERROR_MSG_NO_PBA_FOUND);
    }

    private void responseValidate(Map<String, Object> orgResponse) {

        assertThat(orgResponse.get("http_status").toString().contains("200"));
        Map<String, Object> activeOrganisation = (Map<String, Object>) orgResponse.get("organisationEntityResponse");

        Map<String, Object> superUser = ((Map<String, Object>) activeOrganisation.get("superUser"));
        assertThat(superUser.get("firstName")).isEqualTo("testFn");
        assertThat(superUser.get("lastName")).isEqualTo("testLn");
        assertThat(superUser.get("email")).isEqualTo("dummy@email.com");
    }

    @Test
    void create_organisation_with_minimal_and_mandatory_parameters_for_int_otherOrgs() {

        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        List<OrgAttributeRequest> orgAttributeRequests = new ArrayList<>();

        OrgAttributeRequest orgAttributeRequest = new OrgAttributeRequest();

        orgAttributeRequest.setKey("testKey");
        orgAttributeRequest.setValue("testValue");

        orgAttributeRequests.add(orgAttributeRequest);

        OrganisationOtherOrgsCreationRequest organisationOtherOrgsCreationRequest = new
            OrganisationOtherOrgsCreationRequest("some-org-name","PENDING","test",
            "sra-id","false","comNum",
            "company-url",aUserCreationRequest()
            .firstName("some-fname")
            .lastName("some-lname")
            .email("someone@somewhere.com")
            .build(),
            paymentAccounts,
            Arrays.asList(aContactInformationCreationRequest()
                .addressLine1("addressLine1")
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
                .build()),"Doctor",orgAttributeRequests);
        Map<String, Object> response =
            professionalReferenceDataClient.createOrganisationIntV2(organisationOtherOrgsCreationRequest);

        String orgIdentifierResponse = (String) response.get(ORG_IDENTIFIER);
        Organisation persistedOrganisation = organisationRepository
            .findByOrganisationIdentifier(orgIdentifierResponse);
        assertThat(persistedOrganisation.getOrganisationIdentifier().toString()).isEqualTo(orgIdentifierResponse);
        assertThat(persistedOrganisation.getOrgType()).isEqualTo("Doctor");
        assertThat(persistedOrganisation.getOrgAttributes().size()).isEqualTo(1);
    }

    @Test
    void returns_error_message_when_org_type_is_null_for_V2_external_otherOrgs() {

        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        List<OrgAttributeRequest> orgAttributeRequests = new ArrayList<>();

        OrgAttributeRequest orgAttributeRequest = new OrgAttributeRequest();

        orgAttributeRequest.setKey("testKey");
        orgAttributeRequest.setValue("testValue");

        orgAttributeRequests.add(orgAttributeRequest);

        OrganisationOtherOrgsCreationRequest organisationOtherOrgsCreationRequest = new
            OrganisationOtherOrgsCreationRequest("some-org-name","PENDING","test",
            "sra-id","false","comNum",
            "company-url",aUserCreationRequest()
            .firstName("some-fname")
            .lastName("some-lname")
            .email("someone@somewhere.com")
            .build(),
            paymentAccounts,
            Arrays.asList(aContactInformationCreationRequest()
                .addressLine1("addressLine1")
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
                .build()),null,orgAttributeRequests);
        Map<String, Object> response =
            professionalReferenceDataClient.createOrganisationV2(organisationOtherOrgsCreationRequest);


        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat((String) response.get("response_body")).contains("orgType must not be null/empty");

    }

    @Test
    void returns_error_message_when_org_type_is_null_for_V2_internal_otherOrgs() {

        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        List<OrgAttributeRequest> orgAttributeRequests = new ArrayList<>();

        OrgAttributeRequest orgAttributeRequest = new OrgAttributeRequest();

        orgAttributeRequest.setKey("testKey");
        orgAttributeRequest.setValue("testValue");

        orgAttributeRequests.add(orgAttributeRequest);

        OrganisationOtherOrgsCreationRequest organisationOtherOrgsCreationRequest = new
            OrganisationOtherOrgsCreationRequest("some-org-name","PENDING","test",
            "sra-id","false","comNum",
            "company-url",aUserCreationRequest()
            .firstName("some-fname")
            .lastName("some-lname")
            .email("someone@somewhere.com")
            .build(),
            paymentAccounts,
            Arrays.asList(aContactInformationCreationRequest()
                .addressLine1("addressLine1")
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
                .build()),null,orgAttributeRequests);
        Map<String, Object> response =
            professionalReferenceDataClient.createOrganisationIntV2(organisationOtherOrgsCreationRequest);


        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat((String) response.get("response_body")).contains("orgType must not be null/empty");

    }
}
