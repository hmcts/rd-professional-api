package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UpdatePbaRequest;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Objects.nonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.PENDING;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.REJECTED;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

class UpdatePaymentAccountsIntegrationTest extends AuthorizationEnabledIntegrationTest {

    Set<String> paymentAccounts = new HashSet<>();
    String pba1 = "PBA2345678";
    String pba2 = "PBA3456789";
    String pba3 = "PBA4567890";

    @Test
    void update_pba_account_numbers_for_an_organisation_200_success_scenario() {
        String orgId = setUpData();

        UpdatePbaRequest updatePbaRequest = new UpdatePbaRequest();
        updatePbaRequest.setPbaRequestList(asList(
                new PbaUpdateRequest(pba1, ACCEPTED.name(), null),
                new PbaUpdateRequest(pba2, REJECTED.name(), ""),
                new PbaUpdateRequest(pba3, ACCEPTED.name(), null)));

        Map<String, Object> updatePbaResponse = professionalReferenceDataClient
                .updatePaymentsAccountsByOrgId(updatePbaRequest, orgId, hmctsAdmin, null);

        assertThat(updatePbaResponse).isNotNull();
        assertThat(updatePbaResponse.get("pbaUpdateStatusResponses")).isNull();
        assertThat(updatePbaResponse.get("http_status")).asString().contains("200");

        List<PaymentAccount> persistedPaymentAccountsAfterUpdate =
                paymentAccountRepository.findByPbaNumberIn(paymentAccounts);

        assertThat(persistedPaymentAccountsAfterUpdate.stream()
                .filter(pba -> REJECTED.equals(pba.getPbaStatus())).count()).isZero();
        assertThat(persistedPaymentAccountsAfterUpdate.stream()
                .filter(pba -> ACCEPTED.equals(pba.getPbaStatus())).count()).isEqualTo(2);
        persistedPaymentAccountsAfterUpdate.forEach(pba ->
                assertThat(pba.getStatusMessage()).isEmpty());
    }

    @Test
    void update_pba_account_numbers_for_an_organisation_200_partial_success_scenario() {
        String orgId = setUpData();

        UpdatePbaRequest updatePbaRequest = new UpdatePbaRequest();
        updatePbaRequest.setPbaRequestList(asList(
                new PbaUpdateRequest("PBA123", ACCEPTED.name(), ""),
                new PbaUpdateRequest(pba2, "INVALID STATUS", ""),
                new PbaUpdateRequest(pba3, ACCEPTED.name(), "")));

        Map<String, Object> updatePbaResponse = professionalReferenceDataClient
                .updatePaymentsAccountsByOrgId(updatePbaRequest, orgId, hmctsAdmin, null);

        assertThat(updatePbaResponse).isNotNull();
        assertThat(updatePbaResponse.get("http_status")).asString().contains("200");
        assertThat(updatePbaResponse.get("message")).asString().contains("Some of the PBAs updated successfully");
        assertThat(updatePbaResponse.get("pbaUpdateStatusResponses")).isNotNull();
        assertThat(updatePbaResponse.get("pbaUpdateStatusResponses").toString())
                .contains("PBA123");
        assertThat(updatePbaResponse.get("pbaUpdateStatusResponses").toString())
                .contains("PBA numbers must start with PBA/pba and be followed by 7 alphanumeric characters");
        assertThat(updatePbaResponse.get("pbaUpdateStatusResponses").toString())
                .contains(pba2);
        assertThat(updatePbaResponse.get("pbaUpdateStatusResponses").toString())
                .contains("Value for Status field is invalid");

        Optional<PaymentAccount> persistedPaymentAccount = paymentAccountRepository.findByPbaNumber(pba3);

        assertTrue(persistedPaymentAccount.isPresent());
        assertThat(persistedPaymentAccount.get().getPbaStatus()).isEqualTo(ACCEPTED);
    }

    @Test
    void update_pba_account_numbers_for_an_organisation_422_failure_scenario() {
        final String orgId = setUpData();

        createOrganisationRequest();

        Optional<PaymentAccount> paymentAccount = paymentAccountRepository.findByPbaNumber(pba3);
        paymentAccount.ifPresent(account -> {
            account.setPbaStatus(ACCEPTED);
            paymentAccountRepository.save(paymentAccount.get());
        });

        UpdatePbaRequest updatePbaRequest = new UpdatePbaRequest();
        updatePbaRequest.setPbaRequestList(asList(
                new PbaUpdateRequest("PBA1234567", ACCEPTED.name(), ""),
                new PbaUpdateRequest(pba3, ACCEPTED.name(), ""),
                new PbaUpdateRequest("PBA123", ACCEPTED.name(), ""),
                new PbaUpdateRequest(pba2, "INVALID STATUS", ""),
                new PbaUpdateRequest("", ACCEPTED.name(), ""),
                new PbaUpdateRequest("PBA1122334", ACCEPTED.name(), ""),
                new PbaUpdateRequest("PBA1239999", null, "")));

        Map<String, Object> updatePbaResponse = professionalReferenceDataClient
                .updatePaymentsAccountsByOrgId(updatePbaRequest, orgId, hmctsAdmin, null);

        assertThat(updatePbaResponse).isNotNull();
        assertThat(updatePbaResponse.get("http_status")).asString().contains("422");
        assertThat(updatePbaResponse.get("response_body").toString())
                .contains("PBA123");
        assertThat(updatePbaResponse.get("response_body").toString())
                .contains("PBA numbers must start with PBA/pba and be followed by 7 alphanumeric characters");
        assertThat(updatePbaResponse.get("response_body").toString())
                .contains("Mandatory field Status missing");
        assertThat(updatePbaResponse.get("response_body").toString())
                .contains(pba2);
        assertThat(updatePbaResponse.get("response_body").toString())
                .contains("Value for Status field is invalid");
        assertThat(updatePbaResponse.get("response_body").toString())
                .contains("Mandatory field PBA number is missing");
        assertThat(updatePbaResponse.get("response_body").toString())
                .contains("PBA1234567");
        assertThat(updatePbaResponse.get("response_body").toString())
                .contains("PBA is not associated with the Organisation");
        assertThat(updatePbaResponse.get("response_body").toString())
                .contains("PBA is not in Pending status");
    }

    @Test
    void update_pba_account_numbers_for_a_pending_organisation_400_failure_scenario() {
        Map<String, Object> createOrganisationResponse =
                professionalReferenceDataClient.createOrganisation(someMinimalOrganisationRequest().build());

        assertThat(createOrganisationResponse.get("http_status")).asString().contains("201");

        String orgId = (String) createOrganisationResponse.get(ORG_IDENTIFIER);

        Map<String, Object> updatePbaResponse = professionalReferenceDataClient
                .updatePaymentsAccountsByOrgId(new UpdatePbaRequest(), orgId, hmctsAdmin, null);

        assertThat(updatePbaResponse).isNotNull();
        assertThat(updatePbaResponse.get("http_status")).asString().contains("400");
    }

    @Test
    void update_pba_account_numbers_with_empty_pba_request_400_failure_scenario() {
        String orgId = setUpData();

        Map<String, Object> updatePbaResponse = professionalReferenceDataClient
                .updatePaymentsAccountsByOrgId(new UpdatePbaRequest(), orgId, hmctsAdmin, null);

        assertThat(updatePbaResponse).isNotNull();
        assertThat(updatePbaResponse.get("http_status")).asString().contains("400");
    }

    @Test
    void update_pba_account_numbers_with_non_prdAdmin_role_403_failure_scenario() {
        Map<String, Object> updatePbaResponse =
                professionalReferenceDataClient.updatePaymentsAccountsByOrgId(new UpdatePbaRequest(),
                        UUID.randomUUID().toString(), puiUserManager, null);

        assertThat(updatePbaResponse).isNotNull();
        assertThat(updatePbaResponse.get("http_status")).asString().contains("403");
    }

    String setUpData() {
        paymentAccounts.addAll(asList(pba1, pba2, pba3));

        OrganisationCreationRequest organisationCreationRequest =
                someMinimalOrganisationRequest().paymentAccount(paymentAccounts).build();

        Map<String, Object> createOrganisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        assertThat(createOrganisationResponse.get("http_status")).asString().contains("201");

        String orgId = (String) createOrganisationResponse.get(ORG_IDENTIFIER);

        OrganisationCreationRequest organisationUpdateRequest =
                organisationRequestWithAllFieldsAreUpdated().status(ACTIVE).build();

        userProfileCreateUserWireMock(HttpStatus.OK);

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, hmctsAdmin, orgId);

        assertThat(responseForOrganisationUpdate.get("http_status")).asString().contains("200");

        List<PaymentAccount> persistedPaymentAccounts = paymentAccountRepository.findByPbaNumberIn(paymentAccounts);

        persistedPaymentAccounts.forEach(pba -> {
            pba.setPbaStatus(PENDING);
            paymentAccountRepository.save(pba);
        });

        return orgId;
    }

    @Test
    void test_updatePaymentsAccounts_should_return_400_when_null_request_body() {
        String orgId = setUpData();

        validateUpdatePaymentsAccountsResponse(orgId, null, HttpStatus.BAD_REQUEST, null);
    }

    @Test
    void test_updatePaymentsAccounts_should_return_400_when_empty_json_object() {
        String orgId = setUpData();
        String pbaRequestBodyEmptyObject = "{}";

        validateUpdatePaymentsAccountsResponse(orgId, pbaRequestBodyEmptyObject, HttpStatus.BAD_REQUEST, null);
    }

    @Test
    void test_updatePaymentsAccounts_should_return_400_when_empty_request_body() {
        String orgId = setUpData();
        String pbaRequestBodyEmpty = "";

        validateUpdatePaymentsAccountsResponse(orgId, pbaRequestBodyEmpty, HttpStatus.BAD_REQUEST, null);
    }

    @Test
    void test_updatePaymentsAccounts_should_return_400_when_request_body_with_space() {
        String orgId = setUpData();
        String pbaRequestBodyWithSpace = " ";

        validateUpdatePaymentsAccountsResponse(orgId, pbaRequestBodyWithSpace, HttpStatus.BAD_REQUEST, null);
    }

    @Test
    void test_updatePaymentsAccounts_should_return_400_when_request_body_with_spec_char() {
        String orgId = setUpData();
        String pbaRequestBodyWithSpecialChar = "*";

        validateUpdatePaymentsAccountsResponse(orgId, pbaRequestBodyWithSpecialChar, HttpStatus.BAD_REQUEST, null);
    }

    @Test
    void test_updatePaymentsAccounts_should_return_400_when_pbaNumbers_array_empty() {
        String orgId = setUpData();
        String pbaRequestBodyEmptyArray = "{\"pbaNumbers\": []}";

        validateUpdatePaymentsAccountsResponse(orgId, pbaRequestBodyEmptyArray, HttpStatus.BAD_REQUEST,
                "No PBAs have been sent in the request");
    }

    @Test
    void test_updatePaymentsAccounts_should_return_400_when_pbaNumbers_misspelled() {
        String orgId = setUpData();
        String pbaRequestBodyMisspelledField = "{\"fghdghdhg\": [\"PBA0000120\"]}";

        validateUpdatePaymentsAccountsResponse(orgId, pbaRequestBodyMisspelledField, HttpStatus.BAD_REQUEST,
                "No PBAs have been sent in the request");
    }

    @Test
    void test_updatePaymentsAccounts_should_return_400_when_pbaNumbers_array_missing() {
        String orgId = setUpData();
        String pbaRequestBodyNoPbaArray = "{\"pbaNumbers\":}";

        validateUpdatePaymentsAccountsResponse(orgId, pbaRequestBodyNoPbaArray, HttpStatus.BAD_REQUEST, null);
    }

    @Test
    void test_updatePaymentsAccounts_should_return_400_when_pbaNumbers_empty() {
        String orgId = setUpData();
        String pbaRequestBodyEmptyPba = "{\"pbaNumbers\": \"\"}";

        validateUpdatePaymentsAccountsResponse(orgId, pbaRequestBodyEmptyPba, HttpStatus.BAD_REQUEST, null);
    }

    @Test
    void test_updatePaymentsAccounts_should_return_400_when_pbaNumbers_null() {
        String orgId = setUpData();
        String pbaRequestBodyPbaNull = "{\"pbaNumbers\": null}";

        validateUpdatePaymentsAccountsResponse(orgId, pbaRequestBodyPbaNull, HttpStatus.BAD_REQUEST, null);
    }

    @Test
    void test_updatePaymentsAccounts_should_return_400_when_pbaNumbers_spec_char() {
        String orgId = setUpData();
        String pbaRequestBodyPbaWithSpecialChar = "{\"pbaNumbers\": \"*\"}";

        validateUpdatePaymentsAccountsResponse(orgId, pbaRequestBodyPbaWithSpecialChar, HttpStatus.BAD_REQUEST, null);
    }

    @Test
    void test_updatePaymentsAccounts_should_return_400_when_pbaNumbers_space() {
        String orgId = setUpData();
        String pbaRequestBodyPbaWithSpace = "{\"pbaNumbers\": \"  \"}";

        validateUpdatePaymentsAccountsResponse(orgId, pbaRequestBodyPbaWithSpace, HttpStatus.BAD_REQUEST, null);
    }

    @Test
    void test_updatePaymentsAccounts_should_return_422_when_empty_status() {
        String orgId = setUpData();
        String pbaRequestBodyEmptyStatus = "{\"pbaNumbers\":"
                                            + "["
                                            + "{"
                                            + "\"pbaNumber\":\"PBA2345678\","
                                            + "\"status\": \"\","
                                            + "\"statusMessage\":\"\""
                                            + "}"
                                            + "]"
                                            + "}";

        validateUpdatePaymentsAccountsResponse(orgId, pbaRequestBodyEmptyStatus, HttpStatus.UNPROCESSABLE_ENTITY,
                "Mandatory field Status missing");
    }

    @Test
    void test_updatePaymentsAccounts_should_return_422_when_status_field_misspelled() {
        String orgId = setUpData();
        String pbaRequestBodyMisspelledField = "{\"pbaNumbers\":"
                                                + "["
                                                + "{"
                                                + "\"pbaNumber\":\"PBA2345678\","
                                                + "\"sstaattuuss\":\"ACCEPTED\","
                                                + "\"statusMessage\":\"\""
                                                + "}"
                                                + "]"
                                                + "}";

        validateUpdatePaymentsAccountsResponse(orgId, pbaRequestBodyMisspelledField, HttpStatus.UNPROCESSABLE_ENTITY,
                "Mandatory field Status missing");
    }

    @Test
    void test_updatePaymentsAccounts_should_return_422_when_status_with_special_char() {
        String orgId = setUpData();
        String pbaRequestBodyStatusWithSpecChar = "{\"pbaNumbers\":"
                                                + "["
                                                + "{"
                                                + "\"pbaNumber\":\"PBA2345678\","
                                                + "\"status\": \"*\","
                                                + "\"statusMessage\":\"\""
                                                + "}"
                                                + "]"
                                                + "}";

        validateUpdatePaymentsAccountsResponse(orgId, pbaRequestBodyStatusWithSpecChar, HttpStatus.UNPROCESSABLE_ENTITY,
                "Value for Status field is invalid");
    }

    @Test
    void test_updatePaymentsAccounts_should_return_422_when_status_with_space() {
        String orgId = setUpData();
        String pbaRequestBodyStatusWithSpace = "{\"pbaNumbers\":"
                                                + "["
                                                + "{"
                                                + "\"pbaNumber\":\"PBA2345678\","
                                                + "\"status\": \"  \","
                                                + "\"statusMessage\":\"\""
                                                + "}"
                                                + "]"
                                                + "}";

        validateUpdatePaymentsAccountsResponse(orgId, pbaRequestBodyStatusWithSpace, HttpStatus.UNPROCESSABLE_ENTITY,
                "Value for Status field is invalid");
    }

    @Test
    void test_updatePaymentsAccounts_should_return_200_when_status_lowercase() {
        String orgId = setUpData();
        String pbaRequestBodyStatusLowercase = "{\"pbaNumbers\":"
                                                + "["
                                                + "{"
                                                + "\"pbaNumber\":\"PBA2345678\","
                                                + "\"status\":\"accepted\","
                                                + "\"statusMessage\":\"\""
                                                + "}"
                                                + "]"
                                                + "}";

        validateUpdatePaymentsAccountsResponse(orgId, pbaRequestBodyStatusLowercase, HttpStatus.OK, null);
    }

    @Test
    void test_updatePaymentsAccounts_should_return_200_when_status_uppercase() {
        String orgId = setUpData();
        String pbaRequestBodyUppercase = "{\"pbaNumbers\":"
                                        + "["
                                        + "{"
                                        + "\"pbaNumber\":\"PBA2345678\","
                                        + "\"status\":\"ACCEPTED\","
                                        + "\"statusMessage\":\"\""
                                        + "}"
                                        + "]"
                                        + "}";

        validateUpdatePaymentsAccountsResponse(orgId, pbaRequestBodyUppercase, HttpStatus.OK, null);
    }

    private void validateUpdatePaymentsAccountsResponse(String orgId, String requestBody,
                                        HttpStatus expectedStatus, String expectedMessage) {
        Map<String, Object> pbaResponse =
                professionalReferenceDataClient.updatePaymentsAccountsByOrgId(null, orgId, hmctsAdmin,
                        requestBody);

        if (expectedStatus == HttpStatus.OK) {
            assertThat(pbaResponse).containsEntry("http_status", "200 OK");
        } else if (expectedStatus == HttpStatus.BAD_REQUEST) {
            assertThat(pbaResponse).containsEntry("http_status", "400");
        } else if (expectedStatus == HttpStatus.UNPROCESSABLE_ENTITY) {
            assertThat(pbaResponse).containsEntry("http_status", "422");
        }

        if (nonNull(expectedMessage)) {
            assertThat(pbaResponse.get("response_body").toString()).contains(expectedMessage);
        }
    }
}