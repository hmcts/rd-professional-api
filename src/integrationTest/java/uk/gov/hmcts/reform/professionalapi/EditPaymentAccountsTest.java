package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.organisationRequestWithAllFields;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

public class EditPaymentAccountsTest extends AuthorizationEnabledIntegrationTest {


    @Test
    public void test_editPaymentAccountsShouldReturn200() {

        List<String> newPaymentAccounts = new ArrayList<>();
        newPaymentAccounts.add("PBA0000003");
        newPaymentAccounts.add("PBA0000004");
        newPaymentAccounts.add("PBA0000005");
        newPaymentAccounts.add("PBA0000006");
        newPaymentAccounts.add("PBA0000007");
        newPaymentAccounts.add("PBA0000008");

        PbaEditRequest pbaEditRequest = PbaEditRequest.anPbaEditRequest().build();
        pbaEditRequest.setPaymentAccounts(newPaymentAccounts);

        String orgId = createActiveOrganisationAndPbaEditRequest();

        Map<String, Object> pbaResponse = professionalReferenceDataClient.editPaymentsAccountsByOrgId(pbaEditRequest, orgId, hmctsAdmin);

        assertThat(pbaResponse.get("http_status")).isEqualTo("200 OK");
        assertThat(pbaResponse.get("statusMessage")).isEqualTo(HttpStatus.OK.getReasonPhrase());

        java.util.Map<String, Object> retrievePaymentAccountsByEmailResponse = professionalReferenceDataClient.findPaymentAccountsByEmail("someone@somewhere.com", hmctsAdmin);

        Map organisationEntityResponse = (Map) retrievePaymentAccountsByEmailResponse.get("organisationEntityResponse");
        List paymentAccount = (List) organisationEntityResponse.get("paymentAccount");
        assertThat(paymentAccount).hasSize(6);
    }

    @Test
    public void test_editPaymentAccountsShouldThrow404IfOrgIdIsNotFound() {
        PbaEditRequest pbaEditRequest = PbaEditRequest.anPbaEditRequest().build();
        Map<String, Object> response = professionalReferenceDataClient.editPaymentsAccountsByOrgId(pbaEditRequest, "this-does-not-exist", hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("404");
    }

    @Test
    public void test_editPaymentAccountsShouldThrow404IfInvalidPbaIsPassed() {
        List<String> newPaymentAccounts = new ArrayList<>();
        newPaymentAccounts.add("this-is-invalid");

        PbaEditRequest pbaEditRequest = PbaEditRequest.anPbaEditRequest().build();
        pbaEditRequest.setPaymentAccounts(newPaymentAccounts);

        String orgId = createActiveOrganisationAndPbaEditRequest();

        Map<String, Object> response = professionalReferenceDataClient.editPaymentsAccountsByOrgId(pbaEditRequest, orgId, hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    private String createActiveOrganisationAndPbaEditRequest() {
        List<String> existingPaymentAccounts = new ArrayList<>();
        existingPaymentAccounts.add("PBA0000001");
        existingPaymentAccounts.add("PBA0000002");

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().paymentAccount(existingPaymentAccounts).build();
        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgId = (String) responseForOrganisationCreation.get("organisationIdentifier");
        updateOrganisation(orgId, hmctsAdmin, ACTIVE);

        return orgId;
    }
}
