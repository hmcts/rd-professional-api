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
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.PaymentAccountServiceImpl;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

public class EditPaymentAccountsTest extends AuthorizationEnabledIntegrationTest {

    @Autowired
    PaymentAccountServiceImpl paymentAccountService;
    @Autowired
    OrganisationServiceImpl organisationService;

    @Test
    public void editPaymentAccountsTest() {
        List<String> existingPaymentAccounts = new ArrayList<>();
        existingPaymentAccounts.add("PBA0000001");
        existingPaymentAccounts.add("PBA0000002");

        List<String> newPaymentAccounts = new ArrayList<>();
        newPaymentAccounts.add("PBA0000003");
        newPaymentAccounts.add("PBA0000004");
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

        Map<String, Object> pbaResponse = professionalReferenceDataClient.editPaymentsAccountsByOrgId(pbaEditRequest, orgId, hmctsAdmin);

        assertThat(pbaResponse.get("http_status")).isEqualTo("200 OK");
        assertThat(pbaResponse.get("statusMessage")).isEqualTo(HttpStatus.OK.getReasonPhrase());

        java.util.Map<String, Object> retrievePaymentAccountsByEmailResponse = professionalReferenceDataClient.findPaymentAccountsByEmail("someone@somewhere.com", hmctsAdmin);

        Map organisationEntityResponse = (Map) retrievePaymentAccountsByEmailResponse.get("organisationEntityResponse");
        List paymentAccount = (List) organisationEntityResponse.get("paymentAccount");
        assertThat(paymentAccount).hasSize(6);
    }
}
