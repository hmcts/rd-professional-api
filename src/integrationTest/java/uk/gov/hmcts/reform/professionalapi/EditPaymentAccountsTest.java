package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;


class EditPaymentAccountsTest extends AuthorizationEnabledIntegrationTest {

    @Test
    void test_editPaymentAccountsShouldReturn200() {
        Set<String> newPaymentAccounts = new HashSet<>();
        newPaymentAccounts.add("PBA0000001");
        newPaymentAccounts.add("PBA0000003");
        newPaymentAccounts.add("PBA0000004");
        newPaymentAccounts.add("PBA0000005");
        newPaymentAccounts.add("PBA0000006");
        newPaymentAccounts.add("PBA0000007");
        newPaymentAccounts.add("PBA0000008");

        PbaRequest pbaEditRequest = new PbaRequest();
        pbaEditRequest.setPaymentAccounts(newPaymentAccounts);

        String orgId = createActiveOrganisationAndPbaEditRequest();

        Map<String, Object> pbaResponse =
                professionalReferenceDataClient.editPaymentsAccountsByOrgId(pbaEditRequest, orgId, hmctsAdmin, null);

        assertThat(pbaResponse.get("http_status")).isEqualTo("200 OK");
        assertThat(pbaResponse.get("statusMessage")).isEqualTo(HttpStatus.OK.getReasonPhrase());

        java.util.Map<String, Object> retrievePaymentAccountsByEmailResponse = professionalReferenceDataClient
                .findPaymentAccountsByEmailFromHeader("someone@somewhere.com", hmctsAdmin);

        Map organisationEntityResponse = (Map) retrievePaymentAccountsByEmailResponse.get("organisationEntityResponse");
        List paymentAccount = (List) organisationEntityResponse.get("paymentAccount");
        assertThat(paymentAccount).hasSize(7);
        assertThat(paymentAccount).containsExactlyInAnyOrderElementsOf(newPaymentAccounts);
    }

    @Test
    void test_editPaymentAccountsShouldThrow404IfOrgIdIsNotFound() {
        PbaRequest pbaEditRequest = new PbaRequest();
        Set<String> newPaymentAccounts = new HashSet<>();
        newPaymentAccounts.add("PBA0000003");
        newPaymentAccounts.add("PBA0000004");
        pbaEditRequest.setPaymentAccounts(newPaymentAccounts);
        Map<String, Object> response = professionalReferenceDataClient.editPaymentsAccountsByOrgId(pbaEditRequest,
                "A7BNM89", hmctsAdmin, null);
        assertThat(response.get("http_status")).isEqualTo("404");
    }

    @Test
    void test_editPaymentAccountsShouldThrow400IfInvalidPbaIsPassed() {
        Set<String> newPaymentAccounts = new HashSet<>();
        newPaymentAccounts.add("this-is-invalid");

        PbaRequest pbaEditRequest = new PbaRequest();
        pbaEditRequest.setPaymentAccounts(newPaymentAccounts);

        String orgId = createActiveOrganisationAndPbaEditRequest();

        Map<String, Object> response = professionalReferenceDataClient.editPaymentsAccountsByOrgId(pbaEditRequest,
                orgId, hmctsAdmin, null);
        assertThat(response.get("http_status")).isEqualTo("400");
    }


    @Test
    void test_editPaymentAccountsShouldDeleteAllAccountsIfEmptyListIsSent() {

        Set<String> newPaymentAccounts = new HashSet<>();
        PbaRequest pbaEditRequest = new PbaRequest();
        pbaEditRequest.setPaymentAccounts(newPaymentAccounts);

        String orgId = createActiveOrganisationAndPbaEditRequest();

        Map<String, Object> pbaResponse = professionalReferenceDataClient.editPaymentsAccountsByOrgId(pbaEditRequest,
                orgId, hmctsAdmin, null);

        assertThat(pbaResponse.get("http_status")).isEqualTo("200 OK");
        assertThat(pbaResponse.get("statusMessage")).isEqualTo(HttpStatus.OK.getReasonPhrase());

        java.util.Map<String, Object> retrievePaymentAccountsByEmailResponse = professionalReferenceDataClient
                .retrieveSingleOrganisation(orgId, hmctsAdmin);

        List paymentAccounts = (List) retrievePaymentAccountsByEmailResponse.get("paymentAccount");
        assertThat(paymentAccounts).hasSize(0);
    }

    @Test
    void test_editPaymentAccountsSldRtn400IfPaymentAccountBelongsToAnotherOrganisation_WithPbaInErrorMessage() {
        Set<String> existingPaymentAccounts = new HashSet<>();
        existingPaymentAccounts.add("PBA0000003");
        existingPaymentAccounts.add("PBA0000004");

        OrganisationCreationRequest firstOrganisationCreationRequest = organisationRequestWithAllFieldsAreUpdated()
                .status("PENDING").paymentAccount(existingPaymentAccounts).sraId("someSra").build();
        java.util.Map<String, Object> responseForFirstOrganisationCreation = professionalReferenceDataClient
                .createOrganisation(firstOrganisationCreationRequest);
        String firstOrgId = (String) responseForFirstOrganisationCreation.get(ORG_IDENTIFIER);
        updateOrganisation(firstOrgId, hmctsAdmin, ACTIVE);

        Set<String> newPaymentAccounts = new HashSet<>();
        newPaymentAccounts.add("PBA0000003");
        newPaymentAccounts.add("PBA0000005");

        PbaRequest pbaEditRequest = new PbaRequest();
        pbaEditRequest.setPaymentAccounts(newPaymentAccounts);

        String orgId = createActiveOrganisationAndPbaEditRequest();

        Map<String, Object> pbaResponse = professionalReferenceDataClient.editPaymentsAccountsByOrgId(pbaEditRequest,
                orgId, hmctsAdmin, null);

        assertThat(pbaResponse.get("http_status")).isEqualTo("400");
        assertThat(pbaResponse.get("response_body").toString()).contains("The PBA numbers you have entered: PBA0000003 "
                + "belongs to another Organisation");
    }

    @Test
    void test_editPaymentAccountsShouldThrow400IfInvalidPbaIsPassed_WithPbaInErrorMessage() {
        Set<String> newPaymentAccounts = new HashSet<>();
        newPaymentAccounts.add("this-is-invalid");

        PbaRequest pbaEditRequest = new PbaRequest();
        pbaEditRequest.setPaymentAccounts(newPaymentAccounts);

        String orgId = createActiveOrganisationAndPbaEditRequest();

        Map<String, Object> response = professionalReferenceDataClient.editPaymentsAccountsByOrgId(pbaEditRequest,
                orgId, hmctsAdmin, null);
        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body").toString()).contains("PBA numbers must start with PBA/pba and be "
                + "followed by 7 alphanumeric characters. The following PBAs entered are invalid: this-is-invalid");
    }

    private String createActiveOrganisationAndPbaEditRequest() {
        Set<String> existingPaymentAccounts = new HashSet<>();
        existingPaymentAccounts.add("PBA0000001");
        existingPaymentAccounts.add("PBA0000002");

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields()
                .paymentAccount(existingPaymentAccounts).build();
        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest);
        String orgId = (String) responseForOrganisationCreation.get(ORG_IDENTIFIER);
        updateOrganisation(orgId, hmctsAdmin, ACTIVE);

        return orgId;
    }

    @Test
    void test_editPaymentAccounts_should_return_200_when_paymentAccounts_array_empty() {
        String orgId = createActiveOrganisationAndPbaEditRequest();
        String paymentAccountEmptyPbaArray = "{\"paymentAccounts\": []}";

        validateEditPaymentAccountsResponse(orgId, paymentAccountEmptyPbaArray, HttpStatus.OK);
        validateRetrievePaymentAccountsResponse(orgId, 0);
    }

    @Test
    void test_editPaymentAccounts_should_return_200_when_paymentAccounts_misspelled() {
        String orgId = createActiveOrganisationAndPbaEditRequest();
        String paymentAccountMisspelled = "{\"paymentAccount\": [\"PBA0000001\"]}";

        validateEditPaymentAccountsResponse(orgId, paymentAccountMisspelled, HttpStatus.OK);
        validateRetrievePaymentAccountsResponse(orgId, 0);
    }

    @Test
    void test_editPaymentAccounts_should_return_200_when_pbas_empty() {
        String orgId = createActiveOrganisationAndPbaEditRequest();
        String paymentAccountEmptyPbas = "{\"paymentAccounts\": [\"\"]}";

        validateEditPaymentAccountsResponse(orgId,paymentAccountEmptyPbas, HttpStatus.OK);
        validateRetrievePaymentAccountsResponse(orgId, 0);
    }

    @Test
    void test_editPaymentAccounts_should_return_200_when_pbas_null() {
        String orgId = createActiveOrganisationAndPbaEditRequest();
        String paymentAccountNullPbas = "{\"paymentAccounts\": null}";

        validateEditPaymentAccountsResponse(orgId, paymentAccountNullPbas, HttpStatus.OK);
        validateRetrievePaymentAccountsResponse(orgId, 0);
    }

    @Test
    void test_editPaymentAccounts_should_return_200_when_lowercase_pbas() {
        String orgId = createActiveOrganisationAndPbaEditRequest();
        String paymentAccountPbasWithSpace = "{\"paymentAccounts\": [\"pba0000001\"]}";

        validateEditPaymentAccountsResponse(orgId, paymentAccountPbasWithSpace, HttpStatus.OK);
        validateRetrievePaymentAccountsResponse(orgId, 1);
    }

    @Test
    void test_editPaymentAccounts_should_return_400_when_paymentAccounts_array_missing() {
        String orgId = createActiveOrganisationAndPbaEditRequest();
        String paymentAccountMissingArray = "{\"paymentAccounts\": }";

        validateEditPaymentAccountsResponse(orgId, paymentAccountMissingArray, HttpStatus.BAD_REQUEST);
    }

    @Test
    void test_editPaymentAccounts_should_return_400_when_pbas_with_special_characters() {
        String orgId = createActiveOrganisationAndPbaEditRequest();
        String paymentAccountSpecialCharsPbas = "{\"paymentAccounts\": [\"*\"]}";

        validateEditPaymentAccountsResponse(orgId, paymentAccountSpecialCharsPbas, HttpStatus.BAD_REQUEST);
    }

    @Test
    void test_editPaymentAccounts_should_return_400_when_pbas_with_empty_space() {
        String orgId = createActiveOrganisationAndPbaEditRequest();
        String paymentAccountPbasWithSpace = "{\"paymentAccounts\": [\"  \" , \" PBA0000001\"]}";

        validateEditPaymentAccountsResponse(orgId, paymentAccountPbasWithSpace, HttpStatus.BAD_REQUEST);
    }

    private void validateEditPaymentAccountsResponse(String orgId, String requestBody, HttpStatus expectedStatus) {
        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .editPaymentsAccountsByOrgId(null, orgId, hmctsAdmin, requestBody);

        if (expectedStatus == HttpStatus.OK) {
            assertThat(pbaResponse).containsEntry("http_status", "200 OK");
        } else if (expectedStatus == HttpStatus.BAD_REQUEST) {
            assertThat(pbaResponse).containsEntry("http_status", "400");
        }
    }

    private void validateRetrievePaymentAccountsResponse(String orgId, int expectedPaymentAccountSize) {
        Map<String, Object> paymentAccountsResponse = professionalReferenceDataClient
                .retrieveSingleOrganisation(orgId, hmctsAdmin);

        var paymentAccounts = (List<PaymentAccount>) paymentAccountsResponse.get("paymentAccount");

        if (expectedPaymentAccountSize > 0) {
            assertThat(paymentAccounts).isNotEmpty().hasSize(1);
        } else {
            assertThat(paymentAccounts).isEmpty();
        }
    }

}
