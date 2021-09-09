package uk.gov.hmcts.reform.professionalapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.AddPbaResponse;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MESSAGE_USER_MUST_BE_ACTIVE;

public class AddPaymentAccountsIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    @SuppressWarnings("unchecked")
    public void test_all_PaymentAccounts_Success_ShouldReturn_201() {
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
    public void test_add_PaymentAccounts_PartialSuccess_Dupl_ShouldReturn_201() {
        Set<String> paymentAccountsToAdd = new HashSet<>();
        paymentAccountsToAdd.add("PBA0000001");
        paymentAccountsToAdd.add("PBA0000002");
        paymentAccountsToAdd.add("PBA0000003");

        PbaEditRequest pbaEditRequest = new PbaEditRequest();
        pbaEditRequest.setPaymentAccounts(paymentAccountsToAdd);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaEditRequest, puiFinanceManager, userId);

        LinkedHashMap reason = (LinkedHashMap)pbaResponse.get("reason");
        ArrayList duplicatePaymentAccountsList = (ArrayList) reason.get("duplicatePaymentAccounts");
        ArrayList invalidPaymentAccountsList = (ArrayList) reason.get("invalidPaymentAccounts");
        Object message = pbaResponse.get("message");

        assertThat(message).isEqualTo(ERROR_MSG_PARTIAL_SUCCESS);
        assertThat(duplicatePaymentAccountsList).contains("PBA0000001", "PBA0000003");
        assertThat(invalidPaymentAccountsList).isNull();
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
    public void test_add_PaymentAccounts_PartialSuccess_Invl_Dup_ShouldReturn_201() {
        Set<String> paymentAccountsToAdd = new HashSet<>();
        paymentAccountsToAdd.add("invalid-test01");
        paymentAccountsToAdd.add("invalid-test02");
        paymentAccountsToAdd.add("PBA0000001");
        paymentAccountsToAdd.add("PBA0000002");
        paymentAccountsToAdd.add("PBA0000003");

        PbaEditRequest pbaEditRequest = new PbaEditRequest();
        pbaEditRequest.setPaymentAccounts(paymentAccountsToAdd);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaEditRequest, puiFinanceManager, userId);

        LinkedHashMap reason = (LinkedHashMap)pbaResponse.get("reason");
        ArrayList duplicatePaymentAccountsList = (ArrayList) reason.get("duplicatePaymentAccounts");
        ArrayList invalidPaymentAccountsList = (ArrayList) reason.get("invalidPaymentAccounts");
        Object message = pbaResponse.get("message");

        assertThat(message).isEqualTo(ERROR_MSG_PARTIAL_SUCCESS);
        assertThat(duplicatePaymentAccountsList).contains("PBA0000001", "PBA0000003");
        assertThat(invalidPaymentAccountsList).contains("invalid-test01", "invalid-test02");
        assertThat(pbaResponse).containsEntry("http_status", "201 CREATED");

        java.util.Map<String, Object> retrievePaymentAccountsByEmailResponse = professionalReferenceDataClient
                .findPaymentAccountsByEmailFromHeader("someone@somewhere.com", hmctsAdmin);

        Map<String, Object> organisationEntityResponse =
                (Map<String, Object>) retrievePaymentAccountsByEmailResponse.get("organisationEntityResponse");
        List<String> paymentAccount = (List<String>) organisationEntityResponse.get("paymentAccount");
        assertThat(paymentAccount)
                .hasSize(8);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_add_PaymentAccounts_PartialSuccess_Invl_ShouldReturn_201() {
        Set<String> paymentAccountsToAdd = new HashSet<>();
        paymentAccountsToAdd.add("invalid-test01");
        paymentAccountsToAdd.add("invalid-test02");
        paymentAccountsToAdd.add("PBA0000002");

        PbaEditRequest pbaEditRequest = new PbaEditRequest();
        pbaEditRequest.setPaymentAccounts(paymentAccountsToAdd);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaEditRequest, puiFinanceManager, userId);

        LinkedHashMap reason = (LinkedHashMap)pbaResponse.get("reason");
        ArrayList duplicatePaymentAccountsList = (ArrayList) reason.get("duplicatePaymentAccounts");
        ArrayList invalidPaymentAccountsList = (ArrayList) reason.get("invalidPaymentAccounts");
        Object message = pbaResponse.get("message");

        assertThat(message).isEqualTo(ERROR_MSG_PARTIAL_SUCCESS);
        assertThat(duplicatePaymentAccountsList).isNull();
        assertThat(invalidPaymentAccountsList).contains("invalid-test01", "invalid-test02");
        assertThat(pbaResponse).containsEntry("http_status", "201 CREATED");

        java.util.Map<String, Object> retrievePaymentAccountsByEmailResponse = professionalReferenceDataClient
                .findPaymentAccountsByEmailFromHeader("someone@somewhere.com", hmctsAdmin);

        Map<String, Object> organisationEntityResponse =
                (Map<String, Object>) retrievePaymentAccountsByEmailResponse.get("organisationEntityResponse");
        List<String> paymentAccount = (List<String>) organisationEntityResponse.get("paymentAccount");
        assertThat(paymentAccount)
                .hasSize(8);
    }

    @Test
    public void test_add_PaymentAccounts_Failure_Invl_ShouldThrow_400() {
        Set<String> paymentAccountsToAdd = new HashSet<>();
        paymentAccountsToAdd.add("invalid-test01");
        paymentAccountsToAdd.add("invalid-test02");

        PbaEditRequest pbaEditRequest = new PbaEditRequest();
        pbaEditRequest.setPaymentAccounts(paymentAccountsToAdd);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient.addPaymentsAccountsByOrgId(pbaEditRequest,
                puiFinanceManager, userId);

        AddPbaResponse addPbaResponse = getResponseObj(pbaResponse);

        assertThat(addPbaResponse.getMessage()).isNull();
        assertThat(addPbaResponse.getReason().getDuplicatePaymentAccounts()).isNull();
        assertThat(addPbaResponse.getReason().getInvalidPaymentAccounts()).contains("invalid-test01", "invalid-test02");
        assertThat(pbaResponse).containsEntry("http_status", "400");
    }

    @Test
    public void test_add_PaymentAccounts_Failure_Dup_ShouldThrow_400() {
        Set<String> paymentAccountsToAdd = new HashSet<>();
        paymentAccountsToAdd.add("PBA0000001");
        paymentAccountsToAdd.add("PBA0000003");

        PbaEditRequest pbaEditRequest = new PbaEditRequest();
        pbaEditRequest.setPaymentAccounts(paymentAccountsToAdd);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient.addPaymentsAccountsByOrgId(pbaEditRequest,
                puiFinanceManager, userId);

        AddPbaResponse addPbaResponse = getResponseObj(pbaResponse);
        assertThat(addPbaResponse.getMessage()).isNull();
        assertThat(addPbaResponse.getReason().getDuplicatePaymentAccounts()).contains("PBA0000001", "PBA0000003");
        assertThat(addPbaResponse.getReason().getInvalidPaymentAccounts()).isNull();
        assertThat(pbaResponse).containsEntry("http_status", "400");
    }

    @Test
    public void test_addPaymentAccounts_Failure_UserIsNotActive_ShouldThrow_400() {
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

    @Test
    @SuppressWarnings("unchecked")
    public void test_add_PaymentAccounts_PartialSuccess_InvlDup_ShouldReturn_201() {
        Set<String> paymentAccountsToAdd = new HashSet<>();
        paymentAccountsToAdd.add("invalid-test01");
        paymentAccountsToAdd.add("invalid-test02");
        paymentAccountsToAdd.add("PBA0000001");
        paymentAccountsToAdd.add("PBA0000002");

        PbaEditRequest pbaEditRequest = new PbaEditRequest();
        pbaEditRequest.setPaymentAccounts(paymentAccountsToAdd);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaEditRequest, puiFinanceManager, userId);

        LinkedHashMap reason = (LinkedHashMap)pbaResponse.get("reason");
        ArrayList duplicatePaymentAccountsList = (ArrayList) reason.get("duplicatePaymentAccounts");
        ArrayList invalidPaymentAccountsList = (ArrayList) reason.get("invalidPaymentAccounts");
        Object message = pbaResponse.get("message");

        assertThat(message).isEqualTo(ERROR_MSG_PARTIAL_SUCCESS);
        assertThat(duplicatePaymentAccountsList).contains("PBA0000001");
        assertThat(invalidPaymentAccountsList).contains("invalid-test01", "invalid-test02");
        assertThat(pbaResponse).containsEntry("http_status", "201 CREATED");

        java.util.Map<String, Object> retrievePaymentAccountsByEmailResponse = professionalReferenceDataClient
                .findPaymentAccountsByEmailFromHeader("someone@somewhere.com", hmctsAdmin);

        Map<String, Object> organisationEntityResponse =
                (Map<String, Object>) retrievePaymentAccountsByEmailResponse.get("organisationEntityResponse");
        List<String> paymentAccount = (List<String>) organisationEntityResponse.get("paymentAccount");
        assertThat(paymentAccount)
                .hasSize(8);
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

    private AddPbaResponse getResponseObj(Map<String, Object> pbaResponse) {
        String reason = (String) pbaResponse.get("response_body");

        ObjectMapper mapper = new ObjectMapper();
        AddPbaResponse addPbaResponse = null;
        try {
            addPbaResponse = mapper.readValue(reason, AddPbaResponse.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return addPbaResponse;
    }


}