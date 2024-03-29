package uk.gov.hmcts.reform.professionalapi;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;

class DeletePaymentAccountsIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    @SuppressWarnings("unchecked")
    void test_deletePaymentAccountsShouldReturn204() {
        Set<String> paymentAccountsToDelete = new HashSet<>();
        paymentAccountsToDelete.add("PBA0000001");
        paymentAccountsToDelete.add("PBA0000003");

        PbaRequest pbaDeleteRequest = new PbaRequest();
        pbaDeleteRequest.setPaymentAccounts(paymentAccountsToDelete);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .deletePaymentsAccountsByOrgId(pbaDeleteRequest, puiFinanceManager, userId);

        assertThat(pbaResponse).containsEntry("http_status", "204 NO_CONTENT");

        java.util.Map<String, Object> retrievePaymentAccountsByEmailResponse = professionalReferenceDataClient
                .findPaymentAccountsByEmailFromHeader("someone@somewhere.com", hmctsAdmin);

        Map<String, Object> organisationEntityResponse =
                (Map<String, Object>) retrievePaymentAccountsByEmailResponse.get("organisationEntityResponse");
        List<String> paymentAccount = (List<String>) organisationEntityResponse.get("paymentAccount");
        assertThat(paymentAccount)
                .hasSize(5)
                .doesNotContainAnyElementsOf(paymentAccountsToDelete);
    }

    @Test
    void test_deletePaymentAccountsShouldThrow403IfUserIsNotActive() {
        PbaRequest pbaDeleteRequest = new PbaRequest();
        Set<String> paymentAccountsToDelete = new HashSet<>();
        paymentAccountsToDelete.add("PBA0000003");
        paymentAccountsToDelete.add("PBA0000004");
        pbaDeleteRequest.setPaymentAccounts(paymentAccountsToDelete);
        String userId = createActiveUserAndOrganisation(false);
        userProfileGetPendingUserWireMock();
        Map<String, Object> response = professionalReferenceDataClient.deletePaymentsAccountsByOrgId(pbaDeleteRequest,
                puiFinanceManager, userId);
        assertThat(response).containsEntry("http_status", "403");
        assertThat(response.get("response_body").toString())
                .contains("User status must be Active to perform this operation");
    }

    @Test
    void test_deletePaymentAccountsShouldThrow400IfNoPbaIsPassed() {
        Set<String> paymentAccountsToDelete = new HashSet<>();

        PbaRequest pbaDeleteRequest = new PbaRequest();
        pbaDeleteRequest.setPaymentAccounts(paymentAccountsToDelete);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> response = professionalReferenceDataClient.deletePaymentsAccountsByOrgId(pbaDeleteRequest,
                puiFinanceManager, userId);
        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString())
                .contains("No PBA number passed in the request");
    }

    @Test
    void test_deletePaymentAccountsShouldThrow400IfInvalidRequestBodyPassed() {

        PbaRequest pbaDeleteRequest = new PbaRequest();
        pbaDeleteRequest.setPaymentAccounts(null);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> response = professionalReferenceDataClient.deletePaymentsAccountsByOrgId(pbaDeleteRequest,
                puiFinanceManager, userId);
        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString())
                .contains("No PBA number passed in the request");
    }

    @Test
    void test_deletePaymentAccountsShouldThrow400IfNullIsPassedAsPba() {
        Set<String> paymentAccountsToDelete = new HashSet<>();
        paymentAccountsToDelete.add(null);

        PbaRequest pbaDeleteRequest = new PbaRequest();
        pbaDeleteRequest.setPaymentAccounts(paymentAccountsToDelete);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> response = professionalReferenceDataClient.deletePaymentsAccountsByOrgId(pbaDeleteRequest,
                puiFinanceManager, userId);
        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString())
                .contains("Invalid PBA number passed in the request");
    }

    @Test
    void test_deletePaymentAccountsShouldThrow400IfPbaIsPassedThatDoesNotBelongToOrganisation() {
        Set<String> paymentAccountsToDelete = new HashSet<>();
        paymentAccountsToDelete.add("PBA6655443");

        PbaRequest pbaDeleteRequest = new PbaRequest();
        pbaDeleteRequest.setPaymentAccounts(paymentAccountsToDelete);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> response = professionalReferenceDataClient.deletePaymentsAccountsByOrgId(pbaDeleteRequest,
                puiFinanceManager, userId);
        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString())
                .contains("The PBA numbers you have entered: PBA6655443 do not belong to this Organisation");
    }

    @Test
    void test_deletePaymentAccountsShouldThrow400IfEmptyValuePassedAsPba() {
        Set<String> paymentAccountsToDelete = new HashSet<>();
        paymentAccountsToDelete.add(StringUtils.EMPTY);

        PbaRequest pbaDeleteRequest = new PbaRequest();
        pbaDeleteRequest.setPaymentAccounts(paymentAccountsToDelete);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> response = professionalReferenceDataClient.deletePaymentsAccountsByOrgId(pbaDeleteRequest,
                puiFinanceManager, userId);
        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString())
                .contains("Invalid PBA number passed in the request");
    }

    @Test
    void test_deletePaymentAccountsShouldThrow400IfPassedPbasContainsNull() {
        Set<String> paymentAccountsToDelete = new HashSet<>();
        paymentAccountsToDelete.add("PBA0000001");
        paymentAccountsToDelete.add(null);

        PbaRequest pbaDeleteRequest = new PbaRequest();
        pbaDeleteRequest.setPaymentAccounts(paymentAccountsToDelete);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> response = professionalReferenceDataClient.deletePaymentsAccountsByOrgId(pbaDeleteRequest,
                puiFinanceManager, userId);
        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString())
                .contains("Invalid PBA number passed in the request");
    }

    @Test
    void test_deletePaymentAccountsSldRtn400IfPbaBelongsToAnotherOrganisation_WithPbaInErrorMessage() {
        Set<String> existingPaymentAccounts = new HashSet<>();
        existingPaymentAccounts.add("PBA0000002");
        existingPaymentAccounts.add("PBA0000009");

        OrganisationCreationRequest firstOrganisationCreationRequest = organisationRequestWithAllFieldsAreUpdated()
                .status("PENDING").paymentAccount(existingPaymentAccounts).sraId("someSra").build();
        java.util.Map<String, Object> responseForFirstOrganisationCreation = professionalReferenceDataClient
                .createOrganisation(firstOrganisationCreationRequest);
        String firstOrgId = (String) responseForFirstOrganisationCreation.get(ORG_IDENTIFIER);
        updateOrganisation(firstOrgId, puiFinanceManager, ACTIVE);

        Set<String> paymentAccountsToDelete = new HashSet<>();
        paymentAccountsToDelete.add("PBA0000002");
        paymentAccountsToDelete.add("PBA0000005");

        PbaRequest pbaDeleteRequest = new PbaRequest();
        pbaDeleteRequest.setPaymentAccounts(paymentAccountsToDelete);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .deletePaymentsAccountsByOrgId(pbaDeleteRequest, puiFinanceManager, userId);

        assertThat(pbaResponse).containsEntry("http_status", "400");
        assertThat(pbaResponse.get("response_body").toString())
                .contains("The PBA numbers you have entered: PBA0000002 "
                + "do not belong to this Organisation");
    }

    @Test
    void test_deletePaymentAccountsShouldThrow400IfInvalidPbaIsPassed_WithPbaInErrorMessage() {
        Set<String> accountsToDelete = new HashSet<>();
        accountsToDelete.add("this-is-invalid");

        PbaRequest pbaDeleteRequest = new PbaRequest();
        pbaDeleteRequest.setPaymentAccounts(accountsToDelete);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> response = professionalReferenceDataClient.deletePaymentsAccountsByOrgId(pbaDeleteRequest,
                puiFinanceManager, userId);
        assertThat(response).containsEntry("http_status", "400");
        assertThat(response.get("response_body").toString()).contains("PBA numbers must start with PBA/pba and be "
                + "followed by 7 alphanumeric characters. The following PBAs entered are invalid: this-is-invalid");
    }

    private String createActiveUserAndOrganisation(boolean isActive) {
        Set<String> existingPaymentAccounts = new HashSet<>();
        existingPaymentAccounts.add("PBA0000001");
        existingPaymentAccounts.add("PBA0000003");
        existingPaymentAccounts.add("PBA0000004");
        existingPaymentAccounts.add("PBA0000005");
        existingPaymentAccounts.add("PBA0000006");
        existingPaymentAccounts.add("PBA0000007");
        existingPaymentAccounts.add("PBA0000008");

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields()
                .paymentAccount(existingPaymentAccounts).build();
        String organisationIdentifier = createOrganisationWithGivenRequest(organisationCreationRequest);
        String userId = updateOrgAndInviteUser(organisationIdentifier, puiFinanceManager);

        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest);
        String orgId = (String) responseForOrganisationCreation.get(ORG_IDENTIFIER);
        if (isActive) {
            updateOrganisation(orgId, hmctsAdmin, ACTIVE);
        }
        return userId;
    }
}
