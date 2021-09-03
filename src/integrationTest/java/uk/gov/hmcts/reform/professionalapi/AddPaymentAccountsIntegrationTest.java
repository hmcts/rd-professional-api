package uk.gov.hmcts.reform.professionalapi;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MESSAGE_USER_MUST_BE_ACTIVE;

public class AddPaymentAccountsIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    @SuppressWarnings("unchecked")
    public void test_addPaymentAccountsShouldReturn201() {
        Set<String> paymentAccountsToAdd = new HashSet<>();
        paymentAccountsToAdd.add("PBA0000002");

        PbaEditRequest pbaEditRequest = new PbaEditRequest();
        pbaEditRequest.setPaymentAccounts(paymentAccountsToAdd);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaEditRequest, puiFinanceManager, userId);

        assertThat(pbaResponse).containsEntry("http_status", "201 CREATED");

        java.util.Map<String, Object> retrievePaymentAccountsByEmailResponse = professionalReferenceDataClient
                .findPaymentAccountsByEmailFromHeader("someone@somewhere.com", hmctsAdmin);

        Map<String, Object> organisationEntityResponse =
                (Map<String, Object>) retrievePaymentAccountsByEmailResponse.get("organisationEntityResponse");
        List<String> paymentAccount = (List<String>) organisationEntityResponse.get("paymentAccount");
        assertThat(paymentAccount)
                .hasSize(8)
                .containsAll(paymentAccountsToAdd);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_addPaymentAccountsPartialShouldReturn201() {
        Set<String> paymentAccountsToAdd = new HashSet<>();
        paymentAccountsToAdd.add("PBA0000001");
        paymentAccountsToAdd.add("PBA0000002");

        PbaEditRequest pbaEditRequest = new PbaEditRequest();
        pbaEditRequest.setPaymentAccounts(paymentAccountsToAdd);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaEditRequest, puiFinanceManager, userId);

        assertThat(pbaResponse).containsEntry("http_status", "201 CREATED");

        java.util.Map<String, Object> retrievePaymentAccountsByEmailResponse = professionalReferenceDataClient
                .findPaymentAccountsByEmailFromHeader("someone@somewhere.com", hmctsAdmin);

        Map<String, Object> organisationEntityResponse =
                (Map<String, Object>) retrievePaymentAccountsByEmailResponse.get("organisationEntityResponse");
        List<String> paymentAccount = (List<String>) organisationEntityResponse.get("paymentAccount");
        assertThat(paymentAccount)
                .hasSize(8)
                .containsAll(paymentAccountsToAdd);
    }

    @Test
    public void test_addPaymentAccountsShouldThrow400IfInvalidPbaIsPassed() {
        Set<String> paymentAccountsToAdd = new HashSet<>();
        paymentAccountsToAdd.add("this-is-invalid");

        PbaEditRequest pbaEditRequest = new PbaEditRequest();
        pbaEditRequest.setPaymentAccounts(paymentAccountsToAdd);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> response = professionalReferenceDataClient.addPaymentsAccountsByOrgId(pbaEditRequest,
                puiFinanceManager, userId);
        assertThat(response).containsEntry("http_status", "400");
    }

    @Test
    public void test_addPaymentAccountsShouldThrow400IfDuplicate() {
        Set<String> paymentAccountsToAdd = new HashSet<>();
        paymentAccountsToAdd.add("PBA0000001");

        PbaEditRequest pbaEditRequest = new PbaEditRequest();
        pbaEditRequest.setPaymentAccounts(paymentAccountsToAdd);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> response = professionalReferenceDataClient.addPaymentsAccountsByOrgId(pbaEditRequest,
                puiFinanceManager, userId);
        assertThat(response).containsEntry("http_status", "400");
    }

    @Test
    public void test_addPaymentAccountsShouldThrow400IfUserIsNotActive() {
        PbaEditRequest pbaEditRequest = new PbaEditRequest();
        Set<String> paymentAccountsToAdd = new HashSet<>();
        paymentAccountsToAdd.add("PBA0000003");

        pbaEditRequest.setPaymentAccounts(paymentAccountsToAdd);
        String userId = createActiveUserAndOrganisation(false);
        userProfileGetPendingUserWireMock();
        Map<String, Object> response = professionalReferenceDataClient.addPaymentsAccountsByOrgId(pbaEditRequest,
                puiFinanceManager, userId);
        assertThat(response).containsEntry("http_status", "403");
        assertThat(response.get("response_body").toString())
                .contains(ERROR_MESSAGE_USER_MUST_BE_ACTIVE);
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