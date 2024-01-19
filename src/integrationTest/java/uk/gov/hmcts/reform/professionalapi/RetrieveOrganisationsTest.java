package uk.gov.hmcts.reform.professionalapi;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrgAttributeRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.BLOCKED;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.DELETED;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.REVIEW;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

@Slf4j
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class RetrieveOrganisationsTest extends AuthorizationEnabledIntegrationTest {
    static final String SINCE_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(SINCE_TIMESTAMP_FORMAT);
    static final int SINCE_PAUSE_SECONDS = 2;

    @SuppressWarnings("unchecked")
    @Test
    void persists_and_returns_organisation_details() {

        String orgIdentifierResponse = createOrganisationRequest("PENDING");
        assertThat(orgIdentifierResponse).isNotEmpty();
        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveSingleOrganisation(orgIdentifierResponse, hmctsAdmin);

        assertThat(orgResponse.get("http_status")).isEqualTo("200 OK");
        assertThat(orgResponse.get(ORG_IDENTIFIER)).isEqualTo(orgIdentifierResponse);

        assertThat(orgResponse.get("name")).isEqualTo("some-org-name");
        assertThat(orgResponse.get("sraId")).isEqualTo("sra-id");
        assertThat(orgResponse.get("sraRegulated")).isEqualTo(false);
        assertThat(orgResponse.get("companyUrl")).isEqualTo("company-url");
        assertThat(orgResponse.get("companyNumber")).isNotNull();
        assertNotNull(orgResponse.get("dateReceived"));
        assertNull(orgResponse.get("dateApproved"));

        Map<String, Object> superUser = ((Map<String, Object>) orgResponse.get("superUser"));
        assertThat(superUser.get("firstName")).isEqualTo("some-fname");
        assertThat(superUser.get("lastName")).isEqualTo("some-lname");
        assertThat(superUser.get("email")).isEqualTo("someone@somewhere.com");

        List<String> accounts = ((List<String>)  orgResponse.get("paymentAccount"));
        assertThat(accounts.size()).isZero();

        List<String> pendingPaymentAccount = ((List<String>)  orgResponse.get("pendingPaymentAccount"));
        assertThat(pendingPaymentAccount.size()).isOne();

        Map<String, Object> contactInfo = ((List<Map<String, Object>>) orgResponse.get("contactInformation")).get(0);
        assertThat(contactInfo.get("addressLine1")).isEqualTo("addressLine1");
        assertThat(contactInfo.get("addressLine2")).isEqualTo("addressLine2");
        assertThat(contactInfo.get("addressLine3")).isEqualTo("addressLine3");
        assertThat(contactInfo.get("county")).isEqualTo("county");
        assertThat(contactInfo.get("country")).isEqualTo("country");
        assertThat(contactInfo.get("townCity")).isEqualTo("town-city");
        assertThat(contactInfo.get("postCode")).isEqualTo("some-post-code");
        assertThat(contactInfo.get("uprn")).isEqualTo("uprn");
        assertNotNull(contactInfo.get("addressId"));
        assertNotNull(contactInfo.get("created"));

        Map<String, Object> dxAddress = ((List<Map<String, Object>>) contactInfo.get("dxAddress")).get(0);
        assertThat(dxAddress.get("dxNumber")).isEqualTo("DX 1234567890");
        assertThat(dxAddress.get("dxExchange")).isEqualTo("dxExchange");

        //RetrieveOrganisationsTest:Received response to retrieve an organisation details
    }

    @Test
    void persists_and_returns_other_organisation_v2_api_details() {

        String orgIdentifierResponse = createOtherOrganisationRequest("PENDING");
        assertThat(orgIdentifierResponse).isNotEmpty();
        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveSingleOrganisationForV2Api(orgIdentifierResponse, hmctsAdmin);

        assertThat(orgResponse.get("http_status")).isEqualTo("200 OK");
        assertThat(orgResponse.get(ORG_IDENTIFIER)).isEqualTo(orgIdentifierResponse);

        assertThat(orgResponse.get("name")).isEqualTo("some-org-name");
        assertThat(orgResponse.get("sraId")).isEqualTo("sra-id");
        assertThat(orgResponse.get("sraRegulated")).isEqualTo(false);
        assertThat(orgResponse.get("companyUrl")).isEqualTo("company-url");
        assertThat(orgResponse.get("companyNumber")).isNotNull();
        assertNotNull(orgResponse.get("dateReceived"));
        assertNull(orgResponse.get("dateApproved"));

        Map<String, Object> superUser = ((Map<String, Object>) orgResponse.get("superUser"));
        assertThat(superUser.get("firstName")).isEqualTo("some-fname");
        assertThat(superUser.get("lastName")).isEqualTo("some-lname");
        assertThat(superUser.get("email")).isEqualTo("someone@somewhere.com");

        List<String> accounts = ((List<String>)  orgResponse.get("paymentAccount"));
        assertThat(accounts.size()).isZero();

        List<String> pendingPaymentAccount = ((List<String>)  orgResponse.get("pendingPaymentAccount"));
        assertThat(pendingPaymentAccount.size()).isOne();

        Map<String, Object> contactInfo = ((List<Map<String, Object>>) orgResponse.get("contactInformation")).get(0);
        assertThat(contactInfo.get("addressLine1")).isEqualTo("addressLine1");
        assertThat(contactInfo.get("addressLine2")).isEqualTo("addressLine2");
        assertThat(contactInfo.get("addressLine3")).isEqualTo("addressLine3");
        assertThat(contactInfo.get("county")).isEqualTo("county");
        assertThat(contactInfo.get("country")).isEqualTo("country");
        assertThat(contactInfo.get("townCity")).isEqualTo("town-city");
        assertThat(contactInfo.get("postCode")).isEqualTo("some-post-code");
        assertThat(contactInfo.get("uprn")).isEqualTo("uprn");
        assertNotNull(contactInfo.get("addressId"));
        assertNotNull(contactInfo.get("created"));


        Map<String, Object> dxAddress = ((List<Map<String, Object>>) contactInfo.get("dxAddress")).get(0);
        assertThat(dxAddress.get("dxNumber")).isEqualTo("DX 1234567890");
        assertThat(dxAddress.get("dxExchange")).isEqualTo("dxExchange");

        //RetrieveOrganisationsTest:Received response to retrieve an organisation details
        assertThat(orgResponse.get("orgType")).isEqualTo("Doctor");
        assertThat(orgResponse.get("orgAttributes")).isNotNull();
    }


    @Test
    @SuppressWarnings("unchecked")
    void persists_and_returns_organisation_with_pagination() {

        String orgIdentifierResponse1 = createOrganisationRequest("PENDING");
        String orgIdentifier2 = createAndActivateOrganisationWithGivenRequest(
            someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());
        String orgIdentifier3 = createOrganisationWithGivenRequest(
            someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());
        String orgIdentifier4 = createOrganisationWithGivenRequest(
            someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());
        String orgIdentifier5 = createOrganisationWithGivenRequest(
            someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());
        assertThat(orgIdentifierResponse1).isNotEmpty();
        assertThat(orgIdentifier2).isNotEmpty();
        assertThat(orgIdentifier3).isNotEmpty();
        assertThat(orgIdentifier4).isNotEmpty();
        assertThat(orgIdentifier5).isNotEmpty();

        Map<String, Object> orgResponse1 =
            professionalReferenceDataClient.retrieveAllOrganisationsWithPagination("1", "2", hmctsAdmin);

        Map<String, Object> orgResponse2 =
            professionalReferenceDataClient.retrieveAllOrganisationsWithPagination("1", "3", hmctsAdmin);

        Map<String, Object> orgResponse3 =
            professionalReferenceDataClient.retrieveAllOrganisationsWithPagination("1", "5", hmctsAdmin);

        int orgResponse1Size = ((List<Organisation>) orgResponse1.get("organisations")).size();
        int orgResponse2Size = ((List<Organisation>) orgResponse2.get("organisations")).size();
        int orgResponse3Size = ((List<Organisation>) orgResponse3.get("organisations")).size();

        assertThat(orgResponse1).containsEntry("http_status","200 OK");
        assertThat(orgResponse2).containsEntry("http_status","200 OK");
        assertThat(orgResponse3).containsEntry("http_status","200 OK");
        assertThat(orgResponse1Size).isEqualTo(2);
        assertThat(orgResponse2Size).isEqualTo(3);
        assertThat(orgResponse3Size).isEqualTo(5);
    }

    @Test
    @SuppressWarnings("unchecked")
    void persists_and_returns_organisation_with_pagination_since() throws InterruptedException {

        String orgIdentifierResponse1 = createOrganisationRequest("PENDING");
        String orgIdentifier2 = createAndActivateOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());

        TimeUnit.SECONDS.sleep(SINCE_PAUSE_SECONDS);
        final LocalDateTime sinceValue = LocalDateTime.now();
        final String since = sinceValue.format(DATE_TIME_FORMATTER);

        String orgIdentifier3 = createOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());
        String orgIdentifier4 = createOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());
        String orgIdentifier5 = createOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());
        assertThat(orgIdentifierResponse1).isNotEmpty();
        assertThat(orgIdentifier2).isNotEmpty();
        assertThat(orgIdentifier3).isNotEmpty();
        assertThat(orgIdentifier4).isNotEmpty();
        assertThat(orgIdentifier5).isNotEmpty();

        Map<String, Object> orgResponse3 = professionalReferenceDataClient
                .retrieveAllOrganisationsWithPaginationSince("1", "5", hmctsAdmin, since);

        int orgResponse3Size = ((List<Organisation>) orgResponse3.get("organisations")).size();

        assertThat(orgResponse3).containsEntry("http_status","200 OK");
        assertThat(orgResponse3Size).isEqualTo(3);
        assertThat((Boolean) orgResponse3.get("moreAvailable")).isEqualTo(false);
        assertThat(((List<HashMap>) orgResponse3.get("organisations")).get(0).get("lastUpdated")).isNotNull();
        assertThat(((List<HashMap>) orgResponse3.get("organisations")).get(1).get("lastUpdated")).isNotNull();
        assertThat(((List<HashMap>) orgResponse3.get("organisations")).get(2).get("lastUpdated")).isNotNull();
        assertThat(((ArrayList)((List<HashMap>) orgResponse3.get("organisations")).get(0)
                .get("organisationProfileIds")).get(0)).isEqualTo("SOLICITOR_PROFILE");
        assertThat(((ArrayList)((List<HashMap>) orgResponse3.get("organisations")).get(1)
                .get("organisationProfileIds")).get(0)).isEqualTo("SOLICITOR_PROFILE");
        assertThat(((ArrayList)((List<HashMap>) orgResponse3.get("organisations")).get(2)
                .get("organisationProfileIds")).get(0)).isEqualTo("SOLICITOR_PROFILE");
    }

    @Test
    @SuppressWarnings("unchecked")
    void persists_and_returns_other_organisation_for_v2_api_with_pagination() {

        String orgIdentifierResponse1 = createOtherOrganisationRequest("PENDING");
        String orgIdentifier2 = createAndActivateOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());
        String orgIdentifier3 = createOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());
        String orgIdentifier4 = createOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());
        String orgIdentifier5 = createOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());
        assertThat(orgIdentifierResponse1).isNotEmpty();
        assertThat(orgIdentifier2).isNotEmpty();
        assertThat(orgIdentifier3).isNotEmpty();
        assertThat(orgIdentifier4).isNotEmpty();
        assertThat(orgIdentifier5).isNotEmpty();

        Map<String, Object> orgResponse1 =
                professionalReferenceDataClient.retrieveAllOrganisationsWithPaginationForV2Api("1", "2", hmctsAdmin);

        Map<String, Object> orgResponse2 =
                professionalReferenceDataClient.retrieveAllOrganisationsWithPaginationForV2Api("1", "3", hmctsAdmin);

        Map<String, Object> orgResponse3 =
                professionalReferenceDataClient.retrieveAllOrganisationsWithPaginationForV2Api("1", "5", hmctsAdmin);

        int orgResponse1Size = ((List<Organisation>) orgResponse1.get("organisations")).size();
        int orgResponse2Size = ((List<Organisation>) orgResponse2.get("organisations")).size();
        int orgResponse3Size = ((List<Organisation>) orgResponse3.get("organisations")).size();

        assertThat(orgResponse1).containsEntry("http_status","200 OK");
        assertThat(orgResponse2).containsEntry("http_status","200 OK");
        assertThat(orgResponse3).containsEntry("http_status","200 OK");
        assertThat(orgResponse1Size).isEqualTo(2);
        assertThat(orgResponse2Size).isEqualTo(3);
        assertThat(orgResponse3Size).isEqualTo(5);
    }

    @Test
    @SuppressWarnings("unchecked")
    void persists_and_returns_organisation_with_default_pagination() {
        String orgIdentifierResponse1 = createOrganisationRequest("PENDING");
        String orgIdentifier2 = createAndActivateOrganisationWithGivenRequest(
            someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());
        String orgIdentifier3 = createOrganisationWithGivenRequest(
            someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());
        String orgIdentifier4 = createOrganisationWithGivenRequest(
            someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());
        String orgIdentifier5 = createOrganisationWithGivenRequest(
            someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());
        assertThat(orgIdentifierResponse1).isNotEmpty();
        assertThat(orgIdentifier2).isNotEmpty();
        assertThat(orgIdentifier3).isNotEmpty();
        assertThat(orgIdentifier4).isNotEmpty();
        assertThat(orgIdentifier5).isNotEmpty();

        Map<String, Object> orgResponseWithDefaultPageSize =
            professionalReferenceDataClient.retrieveAllOrganisationsWithPagination("1", null, hmctsAdmin);

        Map<String, Object> orgResponseWithDefaultPage =
            professionalReferenceDataClient.retrieveAllOrganisationsWithPagination(null, "2", hmctsAdmin);

        int orgResponse1Size = ((List<Organisation>) orgResponseWithDefaultPageSize.get("organisations")).size();
        int orgResponse2Size = ((List<Organisation>) orgResponseWithDefaultPage.get("organisations")).size();

        assertThat(orgResponseWithDefaultPageSize).containsEntry("http_status","200 OK");
        assertThat(orgResponseWithDefaultPage).containsEntry("http_status","200 OK");
        assertThat(orgResponse1Size).isEqualTo(5);
        assertThat(orgResponse2Size).isEqualTo(2);
    }

    @Test
    void retrieve_organisations_with_invalid_pagination() {
        String orgIdentifierResponse = createOrganisationRequest("PENDING");
        assertThat(orgIdentifierResponse).isNotEmpty();

        Map<String, Object> orgResponse =
            professionalReferenceDataClient.retrieveAllOrganisationsWithPagination("0", null, hmctsAdmin);

        assertThat(orgResponse).containsEntry("http_status", "400");
        assertThat(orgResponse.get("response_body").toString())
            .contains("Default page number should start with page 1");
    }

    @Test
    void retrieve_other_organisations_for_v2_api_with_invalid_pagination() {
        String orgIdentifierResponse = createOtherOrganisationRequest("PENDING");
        assertThat(orgIdentifierResponse).isNotEmpty();

        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisationsWithPaginationForV2Api("0", null, hmctsAdmin);

        assertThat(orgResponse).containsEntry("http_status", "400");
        assertThat(orgResponse.get("response_body").toString())
                .contains("Default page number should start with page 1");
    }

    @Test
    void retrieve_organisations_with_invalid_pagination_size() {
        String orgIdentifierResponse = createOrganisationRequest("PENDING");
        assertThat(orgIdentifierResponse).isNotEmpty();

        Map<String, Object> orgResponse =
            professionalReferenceDataClient.retrieveAllOrganisationsWithPagination("1", "0", hmctsAdmin);

        assertThat(orgResponse).containsEntry("http_status", "400");
        assertThat(orgResponse.get("response_body").toString())
            .contains("Page size must not be less than one");
    }

    @Test
    void retrieve_other_organisations_for_v2_api_with_invalid_pagination_size() {
        String orgIdentifierResponse = createOtherOrganisationRequest("PENDING");
        assertThat(orgIdentifierResponse).isNotEmpty();

        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisationsWithPaginationForV2Api("1", "0", hmctsAdmin);

        assertThat(orgResponse).containsEntry("http_status", "400");
        assertThat(orgResponse.get("response_body").toString())
                .contains("Page size must not be less than one");
    }

    @Test
    void return_organisation_payload_with_200_status_code_for_pui_case_manager_user_organisation_id() {
        String userId = settingUpOrganisation(puiCaseManager);
        Map<String, Object> response = professionalReferenceDataClient.retrieveExternalOrganisation(userId,
                puiCaseManager);
        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(response.get(ORG_IDENTIFIER)).isNotNull();
    }

    @Test
    void return_other_organisation_for_v2_api_payload_with_200_status_code_for_pui_case_manager_user_organisation_id() {
        String userId = settingUpOtherOrganisation(puiCaseManager);
        Map<String, Object> response = professionalReferenceDataClient.retrieveExternalOrganisationForV2Api(userId,
                puiCaseManager);
        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(response.get(ORG_IDENTIFIER)).isNotNull();
    }

    @Test
    void return_organisation_payload_with_200_status_code_with_pending_pbas() {
        String organisationIdentifier = createOrganisationRequest("PENDING");
        assertThat(organisationIdentifier).isNotNull();

        String userId = updateOrgAndInviteUser(organisationIdentifier, puiCaseManager);
        assertThat(userId).isNotNull();

        Optional<PaymentAccount> orgPbas = paymentAccountRepository.findByPbaNumber("PBA1234567");
        assertThat(orgPbas).isPresent();
        PaymentAccount pendingPba = orgPbas.get();
        pendingPba.setPbaStatus(PbaStatus.PENDING);
        paymentAccountRepository.save(pendingPba);

        Map<String, Object> response =
                professionalReferenceDataClient.retrieveExternalOrganisationWithPendingPbas(userId, "PENDING",
                puiCaseManager);
        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(response.get(ORG_IDENTIFIER)).isNotNull();
        assertThat(response.get("pendingPaymentAccount")).isNotNull();
    }

    @Test
    void return_organisation_payload_with_200_status_code_with_pending_pbas_v2() {
        String organisationIdentifier = createOtherOrganisationRequest("PENDING");
        assertThat(organisationIdentifier).isNotNull();

        String userId = updateOrgAndInviteUser(organisationIdentifier, puiCaseManager);
        assertThat(userId).isNotNull();

        Optional<PaymentAccount> orgPbas = paymentAccountRepository.findByPbaNumber("PBA1234567");
        assertThat(orgPbas).isPresent();
        PaymentAccount pendingPba = orgPbas.get();
        pendingPba.setPbaStatus(PbaStatus.PENDING);
        paymentAccountRepository.save(pendingPba);

        Map<String, Object> response =
                professionalReferenceDataClient.retrieveExternalOrganisationWithPendingPbasForV2Api(userId, "PENDING",
                        puiCaseManager);
        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(response.get(ORG_IDENTIFIER)).isNotNull();
        assertThat(response.get("pendingPaymentAccount")).isNotNull();
        assertThat(response.get("orgType")).isEqualTo("Doctor");
        assertThat(response.get("orgAttributes")).isNotNull();
    }

    @Test
    void return_organisation_payload_with_200_status_code_for_pui_finance_manager_user_organisation_id() {
        String userId = settingUpOrganisation(puiFinanceManager);
        Map<String, Object> response = professionalReferenceDataClient.retrieveExternalOrganisation(userId,
                puiFinanceManager);
        assertThat(response.get("http_status").toString().contains(OK.name()));
        assertThat(response.get(ORG_IDENTIFIER)).isNotNull();
    }

    @Test
    void return_other_organisation_v2_api_payload_with_200_status_code_for_pui_finance_manager_user_organisation_id() {
        String userId = settingUpOtherOrganisation(puiFinanceManager);
        Map<String, Object> response = professionalReferenceDataClient.retrieveExternalOrganisationForV2Api(userId,
                puiFinanceManager);
        assertThat(response.get("http_status").toString().contains(OK.name()));
        assertThat(response.get(ORG_IDENTIFIER)).isNotNull();
    }

    @Test
    void return_organisation_payload_with_200_status_code_for_pui_organisation_manager_user_organisation_id() {
        String userId = settingUpOrganisation(puiOrgManager);
        Map<String, Object> response = professionalReferenceDataClient.retrieveExternalOrganisation(userId,
                puiOrgManager);
        assertThat(response.get("http_status").toString().contains(OK.name()));
        assertThat(response.get(ORG_IDENTIFIER)).isNotNull();
    }

    @Test
    void return_other_organisation_payload_with_200_status_code_for_pui_organisation_manager_user_organisation_id() {
        String userId = settingUpOtherOrganisation(puiOrgManager);
        Map<String, Object> response = professionalReferenceDataClient.retrieveExternalOrganisationForV2Api(userId,
                puiOrgManager);
        assertThat(response.get("http_status").toString().contains(OK.name()));
        assertThat(response.get(ORG_IDENTIFIER)).isNotNull();
    }

    @Test
    void persists_and_returns_all_organisations() {

        Set<String> paymentAccounts2ndOrg = new HashSet<>();
        paymentAccounts2ndOrg.add("PBA1000000");
        paymentAccounts2ndOrg.add("PBA1200000");
        paymentAccounts2ndOrg.add("PBA1230000");
        Set<String> paymentAccounts3rdOrg = new HashSet<>();
        paymentAccounts3rdOrg.add("PBA1234567");
        paymentAccounts3rdOrg.add("PBA1234568");
        paymentAccounts3rdOrg.add("PBA1234569");
        paymentAccounts3rdOrg.add("PBA1234561");
        List<DxAddressCreationRequest> dxAddresses = new ArrayList<>();
        DxAddressCreationRequest dx = new DxAddressCreationRequest("NI 1234567890",
                "dxExchange1");
        DxAddressCreationRequest dx2 = new DxAddressCreationRequest("NI 1200000000",
                "dxExchange2");
        dxAddresses.add(dx);
        dxAddresses.add(dx2);
        List<ContactInformationCreationRequest> contactInfoList2 = new ArrayList<>();
        List<ContactInformationCreationRequest> contactInfoList3 = new ArrayList<>();
        contactInfoList2.add(aContactInformationCreationRequest().addressLine1("SECOND org")
                .uprn("uprn")
                .dxAddress(dxAddresses).build());
        contactInfoList3.add(aContactInformationCreationRequest()
                .addressLine1("THIRD org")
                .uprn("uprn")
                .build());
        contactInfoList3.add(aContactInformationCreationRequest()
                .addressLine1("THIRD org 2nd address")
                .uprn("uprn")
                .build());
        contactInfoList3.add(aContactInformationCreationRequest().addressLine1("THIRD org 3rd address").build());

        Map<String, Object> orgResponse1 = professionalReferenceDataClient
                .createOrganisation(someMinimalOrganisationRequest()
                .build());
        String activeOrgResponse2 = createAndActivateOrganisationWithGivenRequest(
                someMinimalOrganisationRequest()
                        .name("some-other-org-name")
                        .status("ACTIVE")
                        .superUser(aUserCreationRequest()
                                .firstName("some-fname")
                                .lastName("some-lname")
                                .email("someoneElse@somewhere.com")
                                .build())
                        .contactInformation(contactInfoList2)
                        .paymentAccount(paymentAccounts2ndOrg)
                        .build());
        Map<String, Object> orgResponse3 = professionalReferenceDataClient
                .createOrganisation(someMinimalOrganisationRequest()
                .name("some-other-org-nam3")
                .superUser(aUserCreationRequest()
                        .firstName("some-fnam3")
                        .lastName("some-lnam3")
                        .email("someoneEls3@somewhere.com")
                        .build())
                .contactInformation(contactInfoList3)
                .paymentAccount(paymentAccounts3rdOrg)
                .build());
        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisations(hmctsAdmin);
        assertThat(orgResponse.get("http_status")).isEqualTo("200 OK");
        assertThat(((List<?>) orgResponse.get("organisations")).size()).isEqualTo(3);

        Map<String, Object> pendingOrganisation1 =
                ((List<Map<String, Object>>) orgResponse.get("organisations")).get(0);
        Map<String, Object> pendingOrganisation2 =
                ((List<Map<String, Object>>) orgResponse.get("organisations")).get(1);
        Map<String, Object> activeOrganisation = ((List<Map<String, Object>>) orgResponse.get("organisations")).get(2);

        Map<String, Object> contactInfo1 =
                ((List<Map<String, Object>>) pendingOrganisation1.get("contactInformation")).get(0);
        Map<String, Object> contactInfo2 =
                ((List<Map<String, Object>>) pendingOrganisation2.get("contactInformation")).get(0);
        Map<String, Object> contactInfo3First
                = ((List<Map<String, Object>>) activeOrganisation.get("contactInformation")).get(0);

        assertThat(pendingOrganisation1.get("name")).isEqualTo("some-org-name");
        assertThat(pendingOrganisation1.get("contactInformation")).asList().size().isEqualTo(1);
        assertThat(pendingOrganisation1.get("paymentAccount")).asList().size().isEqualTo(0);
        assertThat(pendingOrganisation1.get("pendingPaymentAccount")).asList().hasSize(0);

        assertThat(pendingOrganisation2.get("name")).isEqualTo("some-other-org-nam3");
        assertThat(pendingOrganisation2.get("contactInformation")).asList().size().isEqualTo(3);
        assertThat(pendingOrganisation2.get("paymentAccount")).asList().size().isEqualTo(0);

        assertThat(activeOrganisation.get("name")).isEqualTo("some-other-org-name");
        assertThat(activeOrganisation.get("paymentAccount")).asList().size().isEqualTo(3);
        assertThat(activeOrganisation.get("contactInformation")).asList().size().isEqualTo(1);

        assertThat(activeOrganisation.get("paymentAccount").toString()).isEqualTo(paymentAccounts2ndOrg.toString());

        assertThat(contactInfo1.get("addressLine1")).isEqualTo("addressLine1");
        assertThat(contactInfo2.get("addressLine1")).isEqualTo("THIRD org");
        assertThat(contactInfo3First.get("addressLine1")).isEqualTo("SECOND org");
        assertThat(contactInfo1.get("uprn")).isEqualTo("uprn");
        assertNotNull(contactInfo1.get("addressId"));
        assertNotNull(contactInfo1.get("created"));
        assertThat(contactInfo2.get("uprn")).isEqualTo("uprn");
        assertNotNull(contactInfo2.get("addressId"));
        assertNotNull(contactInfo2.get("created"));
        assertThat(contactInfo3First.get("uprn")).isEqualTo("uprn");
        assertNotNull(contactInfo3First.get("addressId"));
        assertNotNull(contactInfo3First.get("created"));

        Map<String, Object> dxAddress = ((List<Map<String, Object>>) contactInfo3First.get("dxAddress")).get(0);
        Map<String, Object> dxAddress2 = ((List<Map<String, Object>>) contactInfo3First.get("dxAddress")).get(1);

        assertThat(dxAddress.get("dxNumber")).isEqualTo("NI 1234567890");
        assertThat(dxAddress.get("dxExchange")).isEqualTo("dxExchange1");
        assertThat(dxAddress2.get("dxNumber")).isEqualTo("NI 1200000000");
        assertThat(dxAddress2.get("dxExchange")).isEqualTo("dxExchange2");
    }

    @Test
    void error_if_organisation_id_invalid() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveSingleOrganisation("123",
                hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    void error_if_organisation_id_not_found() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveSingleOrganisation("11AA116",
                hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("404");
    }

    @Test
    void forbidden_if_pui_case_manager_user_try_access_organisation_id_without_role_access() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveExternalOrganisation("11AA116",
                puiCaseManager);
        assertThat(response.get("http_status")).isEqualTo("403");
    }

    @Test
    void forbidden_if_pui_user_manager_try_access_organisation_id_without_role_access() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveExternalOrganisation("11AA116",
                puiUserManager);
        assertThat(response.get("http_status")).isEqualTo("403");
    }

    @Test
    void forbidden_if_user_does_not_exist_in_org_pui_finance_manager_try_access_organisation_id() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveExternalOrganisation("11AA116",
                puiFinanceManager);
        assertThat(response.get("http_status")).isEqualTo("403");
    }

    @Test
    void persists_and_returns_all_organisations_details_by_pending_status() {

        String organisationIdentifier = createOrganisationRequest("PENDING");
        assertThat(organisationIdentifier).isNotEmpty();
        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisationDetailsByStatusTest(OrganisationStatus.PENDING
                        .name(), hmctsAdmin);
        assertThat(orgResponse.get("organisations")).isNotNull();
        assertThat(orgResponse.get("organisations")).asList().isNotEmpty();
        assertThat(orgResponse.get("http_status").toString().contains("OK"));
    }

    @Test
    public void persists_and_returns_all_organisations_details_by_pending_and_active_status() {

        String organisationIdentifier = createOrganisationRequest("PENDING");
        String organisationIdentifier1 = createAndActivateOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());

        assertThat(organisationIdentifier).isNotEmpty();
        assertThat(organisationIdentifier1).isNotEmpty();

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .retrieveAllOrganisationDetailsByStatusTest("PENDING,ACTIVE", hmctsAdmin);

        assertThat(orgResponse.get("organisations")).isNotNull();
        assertThat(orgResponse.get("organisations")).asList().isNotEmpty();
        assertThat(orgResponse.get("organisations")).asList().size().isEqualTo(2);
        assertThat(orgResponse.get("organisations").toString()).contains("status=PENDING");
        assertThat(orgResponse.get("organisations").toString()).contains("status=ACTIVE");

        assertThat(orgResponse.get("http_status").toString().contains("OK"));
    }

    @Test
    public void persists_and_returns_all_organisations_details_by_pending_and_active_status_since()
            throws InterruptedException {

        String organisationIdentifier = createOrganisationRequest("PENDING");
        TimeUnit.SECONDS.sleep(SINCE_PAUSE_SECONDS);
        final LocalDateTime sinceValue = LocalDateTime.now();
        final String since = sinceValue.format(DATE_TIME_FORMATTER);

        String organisationIdentifier1 = createAndActivateOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());

        assertThat(organisationIdentifier).isNotEmpty();
        assertThat(organisationIdentifier1).isNotEmpty();


        Map<String, Object> orgResponse = professionalReferenceDataClient
                .retrieveAllOrganisationDetailsByStatusSinceTest("PENDING,ACTIVE", hmctsAdmin, since);

        assertThat(orgResponse.get("organisations")).isNotNull();
        assertThat(orgResponse.get("organisations")).asList().isNotEmpty();
        assertThat(orgResponse.get("organisations")).asList().size().isEqualTo(1);
        assertThat(((List<HashMap>) orgResponse.get("organisations")).get(0).get("lastUpdated")).isNotNull();
        assertThat(((ArrayList)((List<HashMap>) orgResponse.get("organisations")).get(0)
                .get("organisationProfileIds")).get(0)).isEqualTo("SOLICITOR_PROFILE");
        assertThat(orgResponse.get("http_status").toString()).contains("OK");
    }

    @Test
    public void persists_and_returns_all_organisations_details_by_pending_and_review_status() {

        String organisationIdentifier = createOrganisationRequest("PENDING");
        String organisationIdentifier1 = createOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().status("REVIEW").sraId(randomAlphabetic(10)).build());


        assertThat(organisationIdentifier).isNotEmpty();
        assertThat(organisationIdentifier1).isNotEmpty();


        Organisation blockedOrg = organisationRepository.findByOrganisationIdentifier(organisationIdentifier1);
        blockedOrg.setStatus(REVIEW);
        organisationRepository.save(blockedOrg);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .retrieveAllOrganisationDetailsByStatusTest("PENDING,REVIEW", hmctsAdmin);

        assertThat(orgResponse.get("organisations")).isNotNull();
        assertThat(orgResponse.get("organisations")).asList().isNotEmpty();
        assertThat(orgResponse.get("organisations")).asList().size().isEqualTo(2);
        assertThat(orgResponse.get("organisations").toString()).contains("status=PENDING");
        assertThat(orgResponse.get("organisations").toString()).contains("status=REVIEW");

        assertThat(orgResponse.get("http_status").toString().contains("OK"));
    }

    @Test
    public void persists_and_returns_all_organisations_details_by_pending_and_blocked_status() {

        String organisationIdentifier = createOrganisationRequest("PENDING");
        String organisationIdentifier1 = createOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().status("BLOCKED").sraId(randomAlphabetic(10)).build());

        assertThat(organisationIdentifier).isNotEmpty();
        assertThat(organisationIdentifier1).isNotEmpty();

        Organisation blockedOrg = organisationRepository.findByOrganisationIdentifier(organisationIdentifier1);
        blockedOrg.setStatus(BLOCKED);
        organisationRepository.save(blockedOrg);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .retrieveAllOrganisationDetailsByStatusTest("PENDING,BLOCKED", hmctsAdmin);

        assertThat(orgResponse.get("organisations")).isNotNull();
        assertThat(orgResponse.get("organisations")).asList().isNotEmpty();
        assertThat(orgResponse.get("organisations")).asList().size().isEqualTo(2);
        assertThat(orgResponse.get("organisations").toString()).contains("status=PENDING");
        assertThat(orgResponse.get("organisations").toString()).contains("status=BLOCKED");

        assertThat(orgResponse.get("http_status").toString().contains("OK"));
    }

    @Test
    public void persists_and_returns_all_organisations_details_by_pending_and_active_status_with_accepted_PBA_only() {

        Set<String> paymentAccounts = Set.of("PBA0000001", "PBA0000002", "PBA0000003");

        String organisationIdentifier = createOrganisationRequest("PENDING");
        String organisationIdentifier1 = createAndActivateOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10))
                        .paymentAccount(paymentAccounts).build());

        assertThat(organisationIdentifier).isNotEmpty();
        assertThat(organisationIdentifier1).isNotEmpty();

        List<PaymentAccount> paymentAccountList = paymentAccountRepository.findByPbaNumberIn(paymentAccounts);

        PaymentAccount pba = paymentAccountList.get(0);
        pba.setPbaStatus(PbaStatus.PENDING);
        paymentAccountRepository.save(pba);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .retrieveAllOrganisationDetailsByStatusTest("PENDING,ACTIVE", hmctsAdmin);

        assertThat(orgResponse.get("organisations")).isNotNull();
        assertThat(orgResponse.get("organisations")).asList().isNotEmpty();
        assertThat(orgResponse.get("organisations")).asList().size().isEqualTo(2);
        assertThat(orgResponse.get("organisations").toString()).contains("status=PENDING");
        assertThat(orgResponse.get("organisations").toString()).contains("status=ACTIVE");

        assertThat(orgResponse.get("http_status").toString()).contains("OK");
    }

    @Test
    public void persists_and_returns_all_organisations_details_by_active_and_blocked_status() {

        String organisationIdentifier = createOrganisationRequest("BLOCKED");
        String organisationIdentifier1 = createAndActivateOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());

        assertThat(organisationIdentifier).isNotEmpty();
        assertThat(organisationIdentifier1).isNotEmpty();

        Organisation org = organisationRepository.findByOrganisationIdentifier(organisationIdentifier);
        org.setStatus(BLOCKED);
        organisationRepository.save(org);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .retrieveAllOrganisationDetailsByStatusTest("BLOCKED,ACTIVE", hmctsAdmin);

        assertThat(orgResponse.get("organisations")).isNotNull();
        assertThat(orgResponse.get("organisations")).asList().isNotEmpty();
        assertThat(orgResponse.get("organisations")).asList().size().isEqualTo(2);
        assertThat(orgResponse.get("organisations").toString()).contains("status=ACTIVE");
        assertThat(orgResponse.get("organisations").toString()).contains("status=BLOCKED");
        assertThat(orgResponse.get("http_status").toString()).contains("OK");
    }

    @Test
    public void persists_and_returns_all_organisations_details_by_active_pending_review_blocked_deleted_status() {

        String organisationIdentifier = createOrganisationRequest("PENDING");
        String organisationIdentifier1 = createAndActivateOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().status("ACTIVE").sraId(randomAlphabetic(10)).build());
        String organisationIdentifier2 = createOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().status("REVIEW").sraId(randomAlphabetic(10)).build());
        String organisationIdentifier3 = createOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().status("BLOCKED").sraId(randomAlphabetic(10)).build());
        String organisationIdentifier4 = createOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().status("DELETED").sraId(randomAlphabetic(10)).build());

        assertThat(organisationIdentifier).isNotEmpty();
        assertThat(organisationIdentifier1).isNotEmpty();
        assertThat(organisationIdentifier2).isNotEmpty();
        assertThat(organisationIdentifier3).isNotEmpty();
        assertThat(organisationIdentifier4).isNotEmpty();

        Organisation org2 = organisationRepository.findByOrganisationIdentifier(organisationIdentifier2);
        org2.setStatus(REVIEW);
        organisationRepository.save(org2);
        Organisation org3 = organisationRepository.findByOrganisationIdentifier(organisationIdentifier3);
        org3.setStatus(BLOCKED);
        organisationRepository.save(org3);
        Organisation org4 = organisationRepository.findByOrganisationIdentifier(organisationIdentifier4);
        org4.setStatus(DELETED);
        organisationRepository.save(org4);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .retrieveAllOrganisationDetailsByStatusTest("PENDING,ACTIVE,BLOCKED,REVIEW,DELETED", hmctsAdmin);

        assertThat(orgResponse.get("organisations")).isNotNull();
        assertThat(orgResponse.get("organisations")).asList().isNotEmpty();
        assertThat(orgResponse.get("organisations")).asList().size().isEqualTo(5);
        assertThat(orgResponse.get("organisations").toString()).contains("status=ACTIVE");
        assertThat(orgResponse.get("organisations").toString()).contains("status=PENDING");
        assertThat(orgResponse.get("organisations").toString()).contains("status=BLOCKED");
        assertThat(orgResponse.get("organisations").toString()).contains("status=REVIEW");
        assertThat(orgResponse.get("organisations").toString()).contains("status=DELETED");
        assertThat(orgResponse.get("http_status").toString()).contains("OK");
    }

    @Test
    public void get_organisations_details_by_invalid_status_param_returns_400() {

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .retrieveAllOrganisationDetailsByStatusTest("INVALID,ACTIVE", hmctsAdmin);

        assertThat(orgResponse.get("http_status").toString()).contains("400");
    }

    @Test
    void persists_and_returns_all_organisations_details_by_active_status() {

        Map<String, Object> orgResponse;
        String organisationIdentifier = createOrganisationRequest("ACTIVE");
        assertThat(organisationIdentifier).isNotEmpty();
        orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisationDetailsByStatusTest(OrganisationStatus.ACTIVE
                        .name(), hmctsAdmin);
        assertThat(orgResponse.get("http_status").toString().contains("OK"));

        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
                .status("ACTIVE").build();
        userProfileCreateUserWireMock(CREATED);
        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest,hmctsAdmin,
                        organisationIdentifier);


        assertThat(responseForOrganisationUpdate.get("http_status")).isEqualTo(200);

        orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisationDetailsByStatusTest(OrganisationStatus.ACTIVE
                        .name(), hmctsAdmin);

        assertThat(orgResponse.get("organisations")).asList().isNotEmpty();
        assertThat(orgResponse.get("http_status").toString().contains("OK"));

        Map<String, Object> activeOrganisation = ((List<Map<String, Object>>) orgResponse.get("organisations")).get(0);

        Map<String, Object> superUser = ((Map<String, Object>) activeOrganisation.get("superUser"));
        assertThat(superUser.get("firstName")).isEqualTo("some-fname");
        assertThat(superUser.get("lastName")).isEqualTo("some-lname");
        assertThat(superUser.get("email")).isEqualTo("someone@somewhere.com");
    }

    @Test
    void persists_and_return_empty_organisation_details_when_no_status_found_in_the_db() {

        String organisationIdentifier = createOrganisationRequest("ACTIVE");
        assertThat(organisationIdentifier).isNotEmpty();
        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisationDetailsByStatusTest(OrganisationStatus
                        .ACTIVE.name(), puiCaseManager);
        assertThat(orgResponse.get("http_status").toString().contains("OK"));
    }

    @Test
    void return_404_when_invalid_status_send_in_the_request_param() {

        String organisationIdentifier = createOrganisationRequest("ACTIVE");
        assertThat(organisationIdentifier).isNotEmpty();
        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisationDetailsByStatusTest("ACTIV", hmctsAdmin);
        assertThat(orgResponse.get("http_status").toString().contains("404"));
    }

    private String createOrganisationRequest(String status) {
        OrganisationCreationRequest organisationCreationRequest = null;
        organisationCreationRequest = organisationRequestWithAllFields().status(status).build();
        Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest);
        return (String) responseForOrganisationCreation.get(ORG_IDENTIFIER);
    }

    private String createOtherOrganisationRequest(String status) {
        OrganisationOtherOrgsCreationRequest organisationCreationRequest = null;
        organisationCreationRequest = otherOrganisationRequestWithAllFields();
        organisationCreationRequest.setStatus(status);
        Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
                .createOrganisationV2(organisationCreationRequest);
        return (String) responseForOrganisationCreation.get(ORG_IDENTIFIER);
    }

    private String createOtherOrganisationRequest() {
        OrganisationOtherOrgsCreationRequest organisationCreationRequest = otherOrganisationRequestWithAllFields();
        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
                .createOrganisationV2(organisationCreationRequest);
        return (String) responseForOrganisationCreation.get(ORG_IDENTIFIER);
    }

    protected String settingUpOtherOrganisation(String role) {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String organisationIdentifier = createOtherOrganisationRequest();
        return updateOrgAndInviteUser(organisationIdentifier, role);
    }



    private OrganisationOtherOrgsCreationRequest otherOrganisationRequestWithAllFields() {

        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        List<OrgAttributeRequest> orgAttributeRequests = new ArrayList<>();

        OrgAttributeRequest orgAttributeRequest = new OrgAttributeRequest();

        orgAttributeRequest.setKey("testKey");
        orgAttributeRequest.setValue("testValue");

        orgAttributeRequests.add(orgAttributeRequest);

        OrganisationOtherOrgsCreationRequest organisationOtherOrgsCreationRequest =
                new OrganisationOtherOrgsCreationRequest("some-org-name",
                        "PENDING",
                        "test",
                        "sra-id",
                        "false",
                        "comNum",
                        "company-url",
                        aUserCreationRequest()
                                .firstName("some-fname")
                                .lastName("some-lname")
                                .email("someone@somewhere.com")
                                .build(),
                        paymentAccounts,
                        Collections
                                .singletonList(aContactInformationCreationRequest()
                                        .addressLine1("addressLine1")
                                        .addressLine2("addressLine2")
                                        .addressLine3("addressLine3")
                                        .country("country")
                                        .county("county")
                                        .townCity("town-city")
                                        .uprn("uprn")
                                        .postCode("some-post-code")
                                        .dxAddress(Collections
                                                .singletonList(dxAddressCreationRequest()
                                                        .dxNumber("DX 1234567890")
                                                        .dxExchange("dxExchange").build()))
                                        .build()),
                        "Doctor",
                        orgAttributeRequests);

        return organisationOtherOrgsCreationRequest;

    }

    @Test
    void retrieve_organisation_should_have_single_super_user() {
        userProfileCreateUserWireMock(CREATED);

        List<String> user1Roles = new ArrayList<>();
        user1Roles.add("pui-user-manager");

        List<String> user2Roles = new ArrayList<>();
        user2Roles.add("pui-user-manager");
        user2Roles.add("organisation-admin");

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();
        Map<String, Object> organisationResponse = professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest);
        String orgIdentifierResponse = (String) organisationResponse.get(ORG_IDENTIFIER);

        professionalReferenceDataClient.updateOrganisation(someMinimalOrganisationRequest().status("ACTIVE").build(),
                hmctsAdmin, orgIdentifierResponse);

        String userIdentifier = retrieveSuperUserIdFromOrganisationId(orgIdentifierResponse);

        userProfileCreateUserWireMock(CREATED);
        professionalReferenceDataClient.addUserToOrganisationWithUserId(orgIdentifierResponse,
                inviteUserCreationRequest("some@email.com", user1Roles), hmctsAdmin, userIdentifier);

        userProfileCreateUserWireMock(CREATED);
        professionalReferenceDataClient.addUserToOrganisationWithUserId(orgIdentifierResponse,
                inviteUserCreationRequest("some@email2.com", user2Roles), hmctsAdmin, userIdentifier);

        Organisation persistedOrganisation = organisationRepository.findByOrganisationIdentifier(orgIdentifierResponse);
        List<ProfessionalUser> persistedUsers = professionalUserRepository.findByOrganisation(persistedOrganisation);
        assertThat(persistedUsers.size()).isEqualTo(3);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .retrieveSingleOrganisation(orgIdentifierResponse, hmctsAdmin);
        assertThat(orgResponse.get("http_status").toString().contains("OK"));
        assertThat(orgResponse.get(ORG_IDENTIFIER)).isEqualTo(orgIdentifierResponse);

        Map<String, Object> superUser = ((Map<String, Object>) orgResponse.get("superUser"));
        assertThat(superUser.get("firstName")).isEqualTo("testFn");
        assertThat(superUser.get("lastName")).isEqualTo("testLn");
        assertThat(superUser.get("email")).isEqualTo("dummy@email.com");

    }

    @Test
    void  persists_and_return_forbidden_when_no_role_associated_with_end_point() {

        String orgIdentifierResponse = createOrganisationRequest("PENDING");
        assertThat(orgIdentifierResponse).isNotEmpty();
        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveSingleOrganisation(orgIdentifierResponse, hmctsAdmin);

        assertThat(orgResponse.get("http_status").toString().contains("403"));

    }

    @Test
    void  persists_and_return_pending_from_prd_and_active_org_details_from_up_and_combine_both() {

        userProfileCreateUserWireMock(CREATED);
        OrganisationCreationRequest organisationRequest = anOrganisationCreationRequest()
                .name("org-name")
                .superUser(aUserCreationRequest()
                        .firstName("fname")
                        .lastName("lname1")
                        .email("someone11@somewhere.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine2").build())).build();

        Map<String, Object> responseForOrganisationCreation
                = professionalReferenceDataClient.createOrganisation(organisationRequest);

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().build();

        Map<String, Object> response = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgId = (String) response.get(ORG_IDENTIFIER);

        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
                .status("ACTIVE").build();

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, hmctsAdmin, orgId);

        assertThat(responseForOrganisationUpdate.get("http_status")).isEqualTo(200);

        Map<String, Object> orgResponse =  professionalReferenceDataClient.retrieveAllOrganisations(hmctsAdmin);

        assertThat(orgResponse.get("http_status").toString().contains("200"));
        assertThat(orgResponse.get("organisations")).asList().isNotEmpty();
        assertThat(orgResponse.get("organisations")).asList().size().isEqualTo(2);

        Map<String, Object> organisationPending = ((List<Map<String, Object>>) orgResponse.get("organisations")).get(0);

        Map<String, Object> superUser = ((Map<String, Object>) organisationPending.get("superUser"));

        assertThat(superUser.get("firstName")).isEqualTo("fname");
        assertThat(superUser.get("lastName")).isEqualTo("lname1");
        assertThat(superUser.get("email")).isEqualTo("someone11@somewhere.com");

        Map<String, Object> organisationActive = ((List<Map<String, Object>>) orgResponse.get("organisations")).get(1);
        assertThat(organisationActive.get("dateReceived")).isNotNull();
        assertThat(organisationActive.get("dateApproved")).isNotNull();

        Map<String, Object> superUserSecond = ((Map<String, Object>) organisationActive.get("superUser"));

        assertThat(superUserSecond.get("firstName")).isEqualTo("some-fname");
        assertThat(superUserSecond.get("lastName")).isEqualTo("some-lname");
        assertThat(superUserSecond.get("email")).isEqualTo("someone@somewhere.com");
    }

    @Test
    void  persists_and_return_an_active_org_with_since() throws InterruptedException {
        userProfileCreateUserWireMock(CREATED);
        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().build();
        professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        TimeUnit.SECONDS.sleep(SINCE_PAUSE_SECONDS);
        final LocalDateTime sinceValue = LocalDateTime.now();
        final String since = sinceValue.format(DATE_TIME_FORMATTER);

        OrganisationCreationRequest organisationRequest = anOrganisationCreationRequest()
                .name("some-org-name-after")
                .superUser(aUserCreationRequest()
                        .firstName("fname")
                        .lastName("lname1")
                        .email("someone11@somewhere.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine2").build())).build();

        Map<String, Object> responseAfter
                = professionalReferenceDataClient.createOrganisation(organisationRequest);

        Map<String, Object> orgResponse =  professionalReferenceDataClient
                .retrieveAllOrganisationsSince(hmctsAdmin, since);

        assertThat(orgResponse.get("http_status").toString()).contains("200");
        assertThat(orgResponse.get("organisations")).asList().isNotEmpty();
        assertThat(orgResponse.get("organisations")).asList().size().isEqualTo(1);
        assertThat(((List<HashMap>) orgResponse.get("organisations")).get(0).get("lastUpdated")).isNotNull();
        assertThat(((ArrayList)((List<HashMap>) orgResponse.get("organisations")).get(0)
                .get("organisationProfileIds")).get(0)).isEqualTo("SOLICITOR_PROFILE");

        Map<String, Object> organisationActive = ((List<Map<String, Object>>) orgResponse.get("organisations")).get(0);

        assertThat(organisationActive.get("name")).isNotNull();
        assertThat(organisationActive.get("name")).isEqualTo("some-org-name-after");
    }

    @Test
    void return_organisation_payload_with_200_status_code_for_pui_user_manager_user_organisation_id() {
        String userId = settingUpOrganisation(puiUserManager);
        Map<String, Object> response = professionalReferenceDataClient
                .retrieveExternalOrganisation(userId, puiUserManager);
        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(response.get(ORG_IDENTIFIER)).isNotNull();
    }

    @Test
    void return_organisation_payload_with_200_status_code_for_pui_caa_manager_user_organisation_id() {
        String userId = settingUpOrganisation(puiCaa);
        Map<String, Object> response = professionalReferenceDataClient.retrieveExternalOrganisation(userId, puiCaa);
        assertThat(response.get("http_status")).isEqualTo("200 OK");
        assertThat(response.get(ORG_IDENTIFIER)).isNotNull();

    }

    @Test
    void return_404_when_invalid_status_send_in_the_request_param_for_v2() {

        String organisationIdentifier = createOrganisationRequest("ACTIVE");
        assertThat(organisationIdentifier).isNotEmpty();
        Map<String, Object> orgResponse =
            professionalReferenceDataClient.retrieveAllOrganisationDetailsByStatusForV2ApiTest("ACTIV", hmctsAdmin);
        assertThat(orgResponse.get("http_status").toString().contains("404"));
    }

    @Test
    void persists_and_return_empty_organisation_details_for_v2_when_no_status_found_in_the_db() {

        String organisationIdentifier = createOtherOrganisationRequest("ACTIVE");
        assertThat(organisationIdentifier).isNotEmpty();
        Map<String, Object> orgResponse =
            professionalReferenceDataClient.retrieveAllOrganisationDetailsByStatusForV2ApiTest(OrganisationStatus
                .ACTIVE.name(), puiCaseManager);
        assertThat(orgResponse.get("http_status").toString().contains("OK"));
    }

    @Test
    public void get_organisations_details_for_v2_by_invalid_status_param_returns_400() {

        Map<String, Object> orgResponse = professionalReferenceDataClient
            .retrieveAllOrganisationDetailsByStatusForV2ApiTest("INVALID,ACTIVE", hmctsAdmin);

        assertThat(orgResponse.get("http_status").toString()).contains("400");
    }


    @Test
    void error_if_organisation_id_invalid_for_v2_api() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveSingleOrganisationForV2Api("123",
            hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    void error_if_organisation_id_invalid_for_v2_ext_api() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveExternalOrganisationForV2Api(null,
            puiCaseManager);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    void error_if_organisation_id_not_found_for_v2_api() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveSingleOrganisationForV2Api("11AA116",
            hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("404");
    }

    @Test
    void forbidden_status_when_user_try_access_organisation_id_without_role_access_for_v2() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveSingleOrganisationForV2Api("11AA116",
            "dummyrole");
        assertThat(response.get("http_status")).isEqualTo("403");
    }

    @Test
    void forbidden_for_v2_if_pui_case_manager_user_try_access_organisation_id_without_role_access() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveExternalOrganisationForV2Api("11AA116",
            puiCaseManager);
        assertThat(response.get("http_status")).isEqualTo("403");
    }

    @Test
    void forbidden_for_v2_if_pui_user_manager_try_access_organisation_id_without_role_access() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveExternalOrganisationForV2Api("11AA116",
            puiUserManager);
        assertThat(response.get("http_status")).isEqualTo("403");
    }

    @Test
    void forbidden_for_v2_if_user_does_not_exist_in_org_pui_finance_manager_try_access_organisation_id() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveExternalOrganisationForV2Api("11AA116",
            puiFinanceManager);
        assertThat(response.get("http_status")).isEqualTo("403");
    }

    @Test
    void forbidden_for_v2_if_user_is_null_in_org_pui_finance_manager_try_access_organisation_id() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveExternalOrganisationForV2Api(null,
            puiFinanceManager);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    void should_return_organisation_details_of_user() {
        String userId = settingUpOrganisation(puiCaseManager);

        Map<String, Object> response = professionalReferenceDataClient.findOrganisationsByUserId(userId, hmctsAdmin);
        assertNotNull(response);
        assertEquals(response.get("name"), "some-org-name1");
    }

    @Test
    void should_throw_bad_request_exception_when_professional_user_is_empty() {
        Map<String, Object> response = professionalReferenceDataClient.findOrganisationsByUserId(null, hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    void should_throw_not_found_exception_when_professional_user_not_found() {
        Map<String, Object> response = professionalReferenceDataClient.findOrganisationsByUserId("123", hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("404");
    }

    @Test
    void forbidden_if_pui_case_manager_user_try_access_organisation_by_user_id_without_role_access() {
        String userId = settingUpOrganisation(puiCaseManager);

        Map<String, Object> response = professionalReferenceDataClient
                .findOrganisationsByUserId(userId, puiCaseManager);
        assertThat(response.get("http_status")).isEqualTo("403");
    }
}
