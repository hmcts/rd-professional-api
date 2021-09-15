package uk.gov.hmcts.reform.professionalapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaAddRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.AddPbaResponse;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.INVALID_REQUEST;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.MALFORMED_JSON;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_PARTIAL_SUCCESS;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MESSAGE_USER_MUST_BE_ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ADD_PBA_REQUEST_EMPTY;

public class AddPaymentAccountsIntegrationTest extends AuthorizationEnabledIntegrationTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    @SuppressWarnings("unchecked")
    public void test_all_PaymentAccounts_Success_ShouldReturn_201() {
        Set<String> paymentAccountsToAdd = new HashSet<>();
        paymentAccountsToAdd.add("PBA0000002");

        PbaAddRequest pbaAddRequest = new PbaAddRequest();
        pbaAddRequest.setPaymentAccounts(paymentAccountsToAdd);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaAddRequest, puiFinanceManager, userId);

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

        PbaAddRequest pbaAddRequest = new PbaAddRequest();
        pbaAddRequest.setPaymentAccounts(paymentAccountsToAdd);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaAddRequest, puiFinanceManager, userId);

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

        PbaAddRequest pbaAddRequest = new PbaAddRequest();
        pbaAddRequest.setPaymentAccounts(paymentAccountsToAdd);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaAddRequest, puiFinanceManager, userId);

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

        PbaAddRequest pbaAddRequest = new PbaAddRequest();
        pbaAddRequest.setPaymentAccounts(paymentAccountsToAdd);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaAddRequest, puiFinanceManager, userId);

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

        PbaAddRequest pbaAddRequest = new PbaAddRequest();
        pbaAddRequest.setPaymentAccounts(paymentAccountsToAdd);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient.addPaymentsAccountsByOrgId(pbaAddRequest,
                puiFinanceManager, userId);

        AddPbaResponse addPbaResponse = convertJsonStringToObj(pbaResponse);

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

        PbaAddRequest pbaAddRequest = new PbaAddRequest();
        pbaAddRequest.setPaymentAccounts(paymentAccountsToAdd);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient.addPaymentsAccountsByOrgId(pbaAddRequest,
                puiFinanceManager, userId);

        AddPbaResponse addPbaResponse = convertJsonStringToObj(pbaResponse);
        assertThat(addPbaResponse.getMessage()).isNull();
        assertThat(addPbaResponse.getReason().getDuplicatePaymentAccounts()).contains("PBA0000001", "PBA0000003");
        assertThat(addPbaResponse.getReason().getInvalidPaymentAccounts()).isNull();
        assertThat(pbaResponse).containsEntry("http_status", "400");
    }

    @Test
    public void test_addPaymentAccounts_Failure_UserIsNotActive_ShouldThrow_400() {
        PbaAddRequest pbaAddRequest = new PbaAddRequest();
        Set<String> paymentAccountsToAdd = new HashSet<>();
        paymentAccountsToAdd.add("PBA0000003");

        pbaAddRequest.setPaymentAccounts(paymentAccountsToAdd);
        String userId = createActiveUserAndOrganisation(false);
        userProfileGetPendingUserWireMock();
        Map<String, Object> response = professionalReferenceDataClient.addPaymentsAccountsByOrgId(pbaAddRequest,
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

        PbaAddRequest pbaAddRequest = new PbaAddRequest();
        pbaAddRequest.setPaymentAccounts(paymentAccountsToAdd);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaAddRequest, puiFinanceManager, userId);

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

    private AddPbaResponse convertJsonStringToObj(Map<String, Object> pbaResponse) {
        String reason = (String) pbaResponse.get("response_body");

        AddPbaResponse addPbaResponse = null;
        try {
            addPbaResponse = mapper.readValue(reason, AddPbaResponse.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return addPbaResponse;
    }

    private ErrorResponse convertJsonToErrorResponseObj(Map<String, Object> pbaResponse) {
        String reason = (String) pbaResponse.get("response_body");

        ErrorResponse errorResponse = null;
        try {
            errorResponse = mapper.readValue(reason, ErrorResponse.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return errorResponse;
    }

    private PbaAddRequest convertJsonReqStringToObj(String request) {
        PbaAddRequest pbaAddRequest = null;
        try {
            pbaAddRequest = mapper.readValue(request, PbaAddRequest.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return pbaAddRequest;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_add_PaymentAccounts_Failure_Has_Single_Null_PBA_ShouldThrow_400() {

        String request = "{\n"
                + "    \"paymentAccounts\": [\n"
                + "        null\n"
                + "    ]\n"
                + "}";

        PbaAddRequest pbaAddRequest = convertJsonReqStringToObj(request);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaAddRequest, puiFinanceManager, userId);

        ErrorResponse errorResponse = convertJsonToErrorResponseObj(pbaResponse);
        assertThat(pbaResponse).containsEntry("http_status", "400");
        assertThat(errorResponse.getErrorDescription()).isEqualTo(ADD_PBA_REQUEST_EMPTY);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(INVALID_REQUEST.getErrorMessage());

    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_add_PaymentAccounts_Failure_has_Empty_Request_ShouldThrow_400() {

        String request = "{\n"
                + "    \"paymentAccounts\": [\n"
                + "    ]\n"
                + "}";

        PbaAddRequest pbaAddRequest = convertJsonReqStringToObj(request);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaAddRequest, puiFinanceManager, userId);

        ErrorResponse errorResponse = convertJsonToErrorResponseObj(pbaResponse);
        assertThat(pbaResponse).containsEntry("http_status", "400");
        assertThat(errorResponse.getErrorDescription()).isEqualTo(ADD_PBA_REQUEST_EMPTY);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(INVALID_REQUEST.getErrorMessage());

    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_add_PaymentAccounts_Failure_has_Empty_Payload_ShouldThrow_400() {

        String request = "{\n"
                + "}";

        PbaAddRequest pbaAddRequest = convertJsonReqStringToObj(request);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaAddRequest, puiFinanceManager, userId);

        ErrorResponse errorResponse = convertJsonToErrorResponseObj(pbaResponse);
        assertThat(pbaResponse).containsEntry("http_status", "400");
        assertThat(errorResponse.getErrorDescription()).isEqualTo(ADD_PBA_REQUEST_EMPTY);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(INVALID_REQUEST.getErrorMessage());

    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_add_PaymentAccounts_Failure_Has_Null_Value_PBA_ShouldThrow_400() {

        String request = "{\n"
                + "    \"paymentAccounts\": [\n"
                + "        \"PBAKQNROCA\",\n"
                + "        \"PBA00000000\",\n"
                + "        \"PBAKQNR1CA\",\n"
                + "        \"PBAC013ABE\",\n"
                + "        null\n"
                + "    ]\n"
                + "}";

        PbaAddRequest pbaAddRequest = convertJsonReqStringToObj(request);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaAddRequest, puiFinanceManager, userId);

        LinkedHashMap reason = (LinkedHashMap)pbaResponse.get("reason");
        ArrayList duplicatePaymentAccountsList = (ArrayList) reason.get("duplicatePaymentAccounts");
        ArrayList invalidPaymentAccountsList = (ArrayList) reason.get("invalidPaymentAccounts");
        Object message = pbaResponse.get("message");

        assertThat(message).isEqualTo(ERROR_MSG_PARTIAL_SUCCESS);
        assertThat(invalidPaymentAccountsList).contains("PBA00000000");
        assertThat(duplicatePaymentAccountsList).isNull();
        assertThat(pbaResponse).containsEntry("http_status", "201 CREATED");

        java.util.Map<String, Object> retrievePaymentAccountsByEmailResponse = professionalReferenceDataClient
                .findPaymentAccountsByEmailFromHeader("someone@somewhere.com", hmctsAdmin);

        Map<String, Object> organisationEntityResponse =
                (Map<String, Object>) retrievePaymentAccountsByEmailResponse.get("organisationEntityResponse");
        List<String> paymentAccount = (List<String>) organisationEntityResponse.get("paymentAccount");
        assertThat(paymentAccount)
                .hasSize(10)
                .contains("PBAKQNROCA","PBAKQNR1CA","PBAC013ABE");

    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_add_PaymentAccounts_Failure_Has_Misspelled_payload_ShouldThrow_400() {

        String request = "{\n"
                + "    \"paymentBccounts\": [\n"
                + "        \"PBAKQNROCA\",\n"
                + "        \"PBA00000000\",\n"
                + "        \"PBAKQNR1CA\",\n"
                + "        \"PBAC013ABE\",\n"
                + "        null\n"
                + "    ]\n"
                + "}";

        PbaAddRequest pbaAddRequest = convertJsonReqStringToObj(request);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaAddRequest, puiFinanceManager, userId);

        ErrorResponse errorResponse = convertJsonToErrorResponseObj(pbaResponse);
        assertThat(pbaResponse).containsEntry("http_status", "400");
        assertThat(errorResponse.getErrorMessage()).isEqualTo(MALFORMED_JSON.getErrorMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_add_PaymentAccounts_Failure_Has_Misspelled02_payload_ShouldThrow_400() {

        String request = "{\n"
                + "    \"PaymentAccounts\": [\n"
                + "        \"PBAKQNROCA\",\n"
                + "    ]\n"
                + "}";

        PbaAddRequest pbaAddRequest = convertJsonReqStringToObj(request);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaAddRequest, puiFinanceManager, userId);

        ErrorResponse errorResponse = convertJsonToErrorResponseObj(pbaResponse);
        assertThat(pbaResponse).containsEntry("http_status", "400");
        assertThat(errorResponse.getErrorMessage()).isEqualTo(MALFORMED_JSON.getErrorMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_add_PaymentAccounts_Failure_Has_Single_empty_PBA_ShouldThrow_400() {

        String request = "{\n"
                + "    \"paymentAccounts\": [\n"
                + "        \"\"\n"
                + "    ]\n"
                + "}";

        PbaAddRequest pbaAddRequest = convertJsonReqStringToObj(request);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaAddRequest, puiFinanceManager, userId);

        ErrorResponse errorResponse = convertJsonToErrorResponseObj(pbaResponse);
        assertThat(pbaResponse).containsEntry("http_status", "400");
        assertThat(errorResponse.getErrorDescription()).isEqualTo(ADD_PBA_REQUEST_EMPTY);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(INVALID_REQUEST.getErrorMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_add_PaymentAccounts_PartialSuccess_Dupl_And_null_ShouldReturn_201() {
        Set<String> paymentAccountsToAdd = new HashSet<>();
        paymentAccountsToAdd.add("PBA0000001");
        paymentAccountsToAdd.add("PBA0000002");
        paymentAccountsToAdd.add("PBA0000003");
        paymentAccountsToAdd.add("invalid-test01");
        paymentAccountsToAdd.add("invalid-test02");

        Set<String> paymentAccountsEmptyOrNullToAdd = new HashSet<>();
        paymentAccountsEmptyOrNullToAdd.add(null);
        paymentAccountsEmptyOrNullToAdd.add(null);
        paymentAccountsEmptyOrNullToAdd.add("");
        paymentAccountsEmptyOrNullToAdd.add(" ");

        PbaAddRequest pbaAddRequest = new PbaAddRequest();
        pbaAddRequest.setPaymentAccounts(paymentAccountsToAdd);
        pbaAddRequest.getPaymentAccounts().addAll(paymentAccountsEmptyOrNullToAdd);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaAddRequest, puiFinanceManager, userId);

        LinkedHashMap reason = (LinkedHashMap)pbaResponse.get("reason");
        ArrayList duplicatePaymentAccountsList = (ArrayList) reason.get("duplicatePaymentAccounts");
        ArrayList invalidPaymentAccountsList = (ArrayList) reason.get("invalidPaymentAccounts");
        Object message = pbaResponse.get("message");

        assertThat(message).isEqualTo(ERROR_MSG_PARTIAL_SUCCESS);
        assertThat(duplicatePaymentAccountsList).contains("PBA0000001", "PBA0000003");
        assertThat(invalidPaymentAccountsList).contains("invalid-test01","invalid-test02");
        assertThat(pbaResponse).containsEntry("http_status", "201 CREATED");

        java.util.Map<String, Object> retrievePaymentAccountsByEmailResponse = professionalReferenceDataClient
                .findPaymentAccountsByEmailFromHeader("someone@somewhere.com", hmctsAdmin);

        Map<String, Object> organisationEntityResponse =
                (Map<String, Object>) retrievePaymentAccountsByEmailResponse.get("organisationEntityResponse");
        List<String> paymentAccount = (List<String>) organisationEntityResponse.get("paymentAccount");
        assertThat(paymentAccount)
                .hasSize(8)
                .contains("PBA0000001","PBA0000001","PBA0000001");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_add_PaymentAccounts_Failure_Has_Single_emptySpace_PBA_ShouldThrow_400() {

        String request = "{\n"
                + "    \"paymentAccounts\": [\n"
                + "        \"  \"\n"
                + "    ]\n"
                + "}";

        PbaAddRequest pbaAddRequest = convertJsonReqStringToObj(request);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaAddRequest, puiFinanceManager, userId);

        ErrorResponse errorResponse = convertJsonToErrorResponseObj(pbaResponse);
        assertThat(pbaResponse).containsEntry("http_status", "400");
        assertThat(errorResponse.getErrorDescription()).isEqualTo(ADD_PBA_REQUEST_EMPTY);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(INVALID_REQUEST.getErrorMessage());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_add_PaymentAccounts_PartialSuccess_Dupl_And_null_02_ShouldReturn_201() {
        String request = "{\n"
                + "    \"paymentAccounts\": [\n"
                + "       \"PBAKQNROCB8\",\n"
                + "        null,\n"
                + "        \"PBAKQNROCB8\",\n"
                + "        null,\n"
                + "        \"PBA3DE3PKJ8\",        \n"
                + "        null,\n"
                + "        \"\",\n"
                + "        \" \"\n"
                + "    ]\n"
                + "}";

        PbaAddRequest pbaAddRequest = convertJsonReqStringToObj(request);

        String userId = createActiveUserAndOrganisation(true);

        Map<String, Object> pbaResponse = professionalReferenceDataClient
                .addPaymentsAccountsByOrgId(pbaAddRequest, puiFinanceManager, userId);

        AddPbaResponse addPbaResponse = convertJsonStringToObj(pbaResponse);
        assertThat(addPbaResponse.getMessage()).isNull();
        assertThat(addPbaResponse.getReason().getDuplicatePaymentAccounts()).isNull();
        assertThat(addPbaResponse.getReason().getInvalidPaymentAccounts()).contains("PBAKQNROCB8", "PBA3DE3PKJ8");
        assertThat(pbaResponse).containsEntry("http_status", "400");
    }

}