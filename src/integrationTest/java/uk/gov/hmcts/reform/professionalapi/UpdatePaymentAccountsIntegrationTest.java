package uk.gov.hmcts.reform.professionalapi;

import org.junit.Test;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.PENDING;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.REJECTED;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

public class UpdatePaymentAccountsIntegrationTest extends AuthorizationEnabledIntegrationTest {

    Set<String> paymentAccounts = new HashSet<>();
    String pba1 = "PBA2345678";
    String pba2 = "PBA3456789";
    String pba3 = "PBA4567890";

    @Test
    public void update_pba_account_numbers_for_an_organisation_200_success_scenario() {
        String orgId = setUpData();

        UpdatePbaRequest updatePbaRequest = new UpdatePbaRequest();
        updatePbaRequest.setPbaRequestList(asList(
                new PbaUpdateRequest(pba1, ACCEPTED.name(), null),
                new PbaUpdateRequest(pba2, REJECTED.name(), ""),
                new PbaUpdateRequest(pba3, ACCEPTED.name(), null)));

        Map<String, Object> updatePbaResponse =
                professionalReferenceDataClient.updatePaymentsAccountsByOrgId(updatePbaRequest, orgId, hmctsAdmin);

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
    public void update_pba_account_numbers_for_an_organisation_200_partial_success_scenario() {
        String orgId = setUpData();

        UpdatePbaRequest updatePbaRequest = new UpdatePbaRequest();
        updatePbaRequest.setPbaRequestList(asList(
                new PbaUpdateRequest("PBA123", ACCEPTED.name(), ""),
                new PbaUpdateRequest(pba2, "INVALID STATUS", ""),
                new PbaUpdateRequest(pba3, ACCEPTED.name(), "")));

        Map<String, Object> updatePbaResponse =
                professionalReferenceDataClient.updatePaymentsAccountsByOrgId(updatePbaRequest, orgId, hmctsAdmin);

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
    public void update_pba_account_numbers_for_an_organisation_422_failure_scenario() {
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

        Map<String, Object> updatePbaResponse =
                professionalReferenceDataClient.updatePaymentsAccountsByOrgId(updatePbaRequest, orgId, hmctsAdmin);

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
    public void update_pba_account_numbers_for_a_pending_organisation_400_failure_scenario() {
        Map<String, Object> createOrganisationResponse =
                professionalReferenceDataClient.createOrganisation(someMinimalOrganisationRequest().build());

        assertThat(createOrganisationResponse.get("http_status")).asString().contains("201");

        String orgId = (String) createOrganisationResponse.get(ORG_IDENTIFIER);

        Map<String, Object> updatePbaResponse = professionalReferenceDataClient
                .updatePaymentsAccountsByOrgId(new UpdatePbaRequest(), orgId, hmctsAdmin);

        assertThat(updatePbaResponse).isNotNull();
        assertThat(updatePbaResponse.get("http_status")).asString().contains("400");
    }

    @Test
    public void update_pba_account_numbers_with_empty_pba_request_400_failure_scenario() {
        String orgId = setUpData();

        Map<String, Object> updatePbaResponse = professionalReferenceDataClient
                .updatePaymentsAccountsByOrgId(new UpdatePbaRequest(), orgId, hmctsAdmin);

        assertThat(updatePbaResponse).isNotNull();
        assertThat(updatePbaResponse.get("http_status")).asString().contains("400");
    }

    @Test
    public void update_pba_account_numbers_with_non_prdAdmin_role_403_failure_scenario() {
        Map<String, Object> updatePbaResponse = professionalReferenceDataClient
                .updatePaymentsAccountsByOrgId(new UpdatePbaRequest(), UUID.randomUUID().toString(), puiUserManager);

        assertThat(updatePbaResponse).isNotNull();
        assertThat(updatePbaResponse.get("http_status")).asString().contains("403");
    }

    public String setUpData() {
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
}