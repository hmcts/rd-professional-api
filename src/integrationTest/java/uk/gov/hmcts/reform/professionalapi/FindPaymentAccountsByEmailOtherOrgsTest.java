package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrgAttributeRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_NO_ORGANISATION_FOUND;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.otherOrganisationRequestWithAllFieldsAreUpdated;

public class FindPaymentAccountsByEmailOtherOrgsTest extends RetrieveOrganisationsTest {

    @Test
    void get_request_returns_correct_payment_accounts_associated_with_email_regOtherOrgs() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);
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

        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisationV2(organisationOtherOrgsCreationRequest);

        String uuid = (String)organisationResponse.get(ORG_IDENTIFIER);

        OrganisationOtherOrgsCreationRequest organisationUpdateRequest =
                otherOrganisationRequestWithAllFieldsAreUpdated();

        organisationUpdateRequest.setStatus("ACTIVE");

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisationForV2Api(organisationUpdateRequest, hmctsAdmin, uuid);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .findPaymentAccountsForV2ByEmailFromHeader("someone@somewhere.com", hmctsAdmin);

        assertThat(orgResponse).isNotEmpty();
        responseValidate_regOtherOrgs(orgResponse);

    }

    @Test
    void returns_multiple_correct_payment_accounts_associated_with_email_regOtherOrgs() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");
        paymentAccounts.add("PBA1234568");

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

        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisationV2(organisationOtherOrgsCreationRequest);

        String uuid = (String)organisationResponse.get(ORG_IDENTIFIER);

        OrganisationOtherOrgsCreationRequest organisationUpdateRequest =
                otherOrganisationRequestWithAllFieldsAreUpdated();

        organisationUpdateRequest.setStatus("ACTIVE");

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisationForV2Api(organisationUpdateRequest, hmctsAdmin, uuid);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .findPaymentAccountsForV2ByEmailFromHeader("someone@somewhere.com", hmctsAdmin);

        assertThat(orgResponse).isNotEmpty();

        responseValidate_regOtherOrgs(orgResponse);
    }

    @Test
    void returns_404_organisation_user_accounts_associated_with_email_and_no_payment_account_regOtherOrgs() {

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
                null,
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

        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisationV2(organisationOtherOrgsCreationRequest);

        String uuid = (String)organisationResponse.get(ORG_IDENTIFIER);

        OrganisationCreationRequest organisationUpdateRequest = otherOrganisationRequestWithAllFieldsAreUpdated();
        organisationUpdateRequest.setStatus("ACTIVE");

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisationForV2Api(organisationUpdateRequest, hmctsAdmin, uuid);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .findPaymentAccountsForV2ByEmailFromHeader("someone@somewhere.com", hmctsAdmin);
        assertThat(orgResponse.get("http_status")).isEqualTo("404");

    }

    @Test
    void return_404_when_organisation_status_pending_for_pba_user_email_address_regOtherOrgs() {

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

        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisationV2(organisationOtherOrgsCreationRequest);

        Map<String, Object> response = professionalReferenceDataClient
                .findPaymentAccountsForV2ByEmailFromHeader("someone@somewhere.com", hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("404");

    }

    @Test
    void returns_404_when_email_not_found_regOtherOrgs() {

        Map<String, Object> response = professionalReferenceDataClient
                .findPaymentAccountsForV2ByEmailFromHeader("wrong@email.com", hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("404");
    }

    @Test
    void get_request_returns_correct_payment_accounts_associated_with_email_fromHeader_internal_regOtherOrgs() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);
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

        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisationV2(organisationOtherOrgsCreationRequest);

        String uuid = (String)organisationResponse.get(ORG_IDENTIFIER);

        OrganisationCreationRequest organisationUpdateRequest = otherOrganisationRequestWithAllFieldsAreUpdated();
        organisationUpdateRequest.setStatus("ACTIVE");

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisationForV2Api(organisationUpdateRequest, hmctsAdmin, uuid);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .findPaymentAccountsForV2ByEmailFromHeader("someone@somewhere.com", hmctsAdmin);

        assertThat(orgResponse).isNotEmpty();
        responseValidate_regOtherOrgs(orgResponse);

    }

    @Test
    void returns_404_when_unkown_email_not_found_fromDb_regOtherOrgs() {

        Map<String, Object> response =
                professionalReferenceDataClient
                        .findPaymentAccountsForV2ByEmailFromHeader("wrong@email.com", hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("404");
    }

    @Test
    void returns_400_whenEmailIsNullInBothHeaderAndParam_regOtherOrgs() {

        Map<String, Object> response =
                professionalReferenceDataClient.findPaymentAccountsForV2ByEmailFromHeader("", hmctsAdmin);

        assertThat(response.get("http_status")).isEqualTo("400");
    }


    @Test
    void get_request_returns_correct_payment_accounts_associated_with_email_fromHeader_external_regOtherOrgs() {
        String userId = settingUpOtherOrganisation(puiUserManager);
        Map<String, Object> orgResponse = professionalReferenceDataClient
                .findPaymentAccountsForV2ByEmailFromHeaderForExternalUsers("someone@somewhere.com",
                        puiUserManager, userId);
        assertThat(orgResponse).isNotEmpty();
        responseValidate_regOtherOrgs(orgResponse);

    }

    @Test
    void get_request_returns_404_when_unknown_email_passed_in_header_external_regOtherOrgs() {
        String userId = settingUpOtherOrganisation(puiUserManager);
        Map<String, Object> orgResponse = professionalReferenceDataClient
                .findPaymentAccountsForV2ByEmailFromHeaderForExternalUsers("dummy23@dummy.com",
                        puiUserManager, userId);
        assertThat(orgResponse.get("http_status")).isEqualTo("404");
        assertThat((String)orgResponse.get("response_body")).contains(ERROR_MSG_NO_ORGANISATION_FOUND);
    }


    private void responseValidate_regOtherOrgs(Map<String, Object> orgResponse) {

        assertThat(orgResponse.get("http_status").toString().contains("200"));
        Map<String, Object> activeOrganisation = (Map<String, Object>) orgResponse.get("organisationEntityResponse");

        Map<String, Object> superUser = ((Map<String, Object>) activeOrganisation.get("superUser"));
        assertThat(superUser.get("firstName")).isEqualTo("testFn");
        assertThat(superUser.get("lastName")).isEqualTo("testLn");
        assertThat(superUser.get("email")).isEqualTo("dummy@email.com");
    }

}
