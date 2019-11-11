package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.organisationRequestWithAllFields;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaResponse;
import uk.gov.hmcts.reform.professionalapi.service.impl.PaymentAccountServiceImpl;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

public class EditPaymentAccountsTest extends AuthorizationEnabledIntegrationTest {

    @Autowired
    PaymentAccountServiceImpl paymentAccountService;

    @Test
    public void editPaymentAccountsTest() {
        List<String> existingPaymentAccounts = new ArrayList<>();
        existingPaymentAccounts.add("PBA0000001");
        existingPaymentAccounts.add("PBA0000002");

        List<String> newPaymentAccounts = new ArrayList<>();
        newPaymentAccounts.add("PBA0000003");
        newPaymentAccounts.add("BBA0000004");
        newPaymentAccounts.add("PBA0000005");
        newPaymentAccounts.add("PBA0000006");
        newPaymentAccounts.add("PBA0000007");
        newPaymentAccounts.add("PBA0000008");

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().paymentAccount(existingPaymentAccounts).build();
        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgId = (String) responseForOrganisationCreation.get("organisationIdentifier");
        updateOrganisation(orgId, hmctsAdmin, ACTIVE);

        PbaEditRequest pbaEditRequest = PbaEditRequest.anPbaEditRequest().build();
        pbaEditRequest.setPaymentAccounts(newPaymentAccounts);

        PbaResponse pbaResponse = paymentAccountService.editPaymentsAccountsByOrgId(pbaEditRequest, orgId);

        assertThat(pbaResponse.getStatusCode()).isEqualTo(HttpStatus.OK.toString());
        assertThat(pbaResponse.getStatusMessage()).isEqualTo(HttpStatus.OK.getReasonPhrase());

        java.util.Map<String, Object> retrievePaymentAccountsByEmailResponse = professionalReferenceDataClient.findPaymentAccountsByEmail("someone@somewhere.com", hmctsAdmin);
        Map<String, Object> organisationEntityResponse = (Map<String, Object>) retrievePaymentAccountsByEmailResponse.get("organisationEntityResponse");
        List<PaymentAccount> paymentAccount = (List<PaymentAccount>) organisationEntityResponse.get("paymentAccount");
        assertThat(paymentAccount).hasSize(6);
    }
}
