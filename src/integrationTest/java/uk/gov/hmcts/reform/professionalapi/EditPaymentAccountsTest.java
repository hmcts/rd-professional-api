package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

public class EditPaymentAccountsTest extends AuthorizationEnabledIntegrationTest {

    @Test
    public void test_editPaymentAccountsShouldReturn200() {
        Set<String> newPaymentAccounts = new HashSet<>();
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
    public void test_editPaymentAccountsShouldThrow400IfOrgIdIsNotFound() {
        PbaEditRequest pbaEditRequest = PbaEditRequest.anPbaEditRequest().build();
        Set<String> newPaymentAccounts = new HashSet<>();
        newPaymentAccounts.add("PBA0000003");
        newPaymentAccounts.add("PBA0000004");
        pbaEditRequest.setPaymentAccounts(newPaymentAccounts);
        Map<String, Object> response = professionalReferenceDataClient.editPaymentsAccountsByOrgId(pbaEditRequest, "this-does-not-exist", hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("400");
    }

    @Test
    public void test_editPaymentAccountsShouldThrow400IfInvalidPbaIsPassed() {
        Set<String> newPaymentAccounts = new HashSet<>();
        newPaymentAccounts.add("this-is-invalid");

        PbaEditRequest pbaEditRequest = PbaEditRequest.anPbaEditRequest().build();
        pbaEditRequest.setPaymentAccounts(newPaymentAccounts);

        String orgId = createActiveOrganisationAndPbaEditRequest();

        Map<String, Object> response = professionalReferenceDataClient.editPaymentsAccountsByOrgId(pbaEditRequest, orgId, hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("400");
    }


    @Test
    public void test_editPaymentAccountsShouldDeleteAllAccountsIfEmptyListIsSent() {

        Set<String> newPaymentAccounts = new HashSet<>();
        PbaEditRequest pbaEditRequest = PbaEditRequest.anPbaEditRequest().build();
        pbaEditRequest.setPaymentAccounts(newPaymentAccounts);

        String orgId = createActiveOrganisationAndPbaEditRequest();

        Map<String, Object> pbaResponse = professionalReferenceDataClient.editPaymentsAccountsByOrgId(pbaEditRequest, orgId, hmctsAdmin);

        assertThat(pbaResponse.get("http_status")).isEqualTo("200 OK");
        assertThat(pbaResponse.get("statusMessage")).isEqualTo(HttpStatus.OK.getReasonPhrase());

        java.util.Map<String, Object> retrievePaymentAccountsByEmailResponse = professionalReferenceDataClient.retrieveSingleOrganisation(orgId, hmctsAdmin);

        List paymentAccounts = (List) retrievePaymentAccountsByEmailResponse.get("paymentAccount");
        assertThat(paymentAccounts).hasSize(0);
    }

    @Test
    public void test_editPaymentAccountsShouldReturn400IfPaymentAccountBelongsToAnotherOrganisation_WithPbaInErrorMessage() {
        Set<String> existingPaymentAccounts = new HashSet<>();
        existingPaymentAccounts.add("PBA0000003");
        existingPaymentAccounts.add("PBA0000004");

        OrganisationCreationRequest firstOrganisationCreationRequest = organisationRequestWithAllFieldsAreUpdated().status("PENDING").paymentAccount(existingPaymentAccounts).build();
        java.util.Map<String, Object> responseForFirstOrganisationCreation = professionalReferenceDataClient.createOrganisation(firstOrganisationCreationRequest);
        String firstOrgId = (String) responseForFirstOrganisationCreation.get("organisationIdentifier");
        updateOrganisation(firstOrgId, hmctsAdmin, ACTIVE);

        Set<String> newPaymentAccounts = new HashSet<>();
        newPaymentAccounts.add("PBA0000003");
        newPaymentAccounts.add("PBA0000005");

        PbaEditRequest pbaEditRequest = PbaEditRequest.anPbaEditRequest().build();
        pbaEditRequest.setPaymentAccounts(newPaymentAccounts);

        String orgId = createActiveOrganisationAndPbaEditRequest();

        Map<String, Object> pbaResponse = professionalReferenceDataClient.editPaymentsAccountsByOrgId(pbaEditRequest, orgId, hmctsAdmin);

        assertThat(pbaResponse.get("http_status")).isEqualTo("400");
        assertThat(pbaResponse.get("response_body").toString()).contains("The PBA numbers you have entered: PBA0000003 belongs to another Organisation");
    }

    @Test
    public void test_editPaymentAccountsShouldThrow400IfInvalidPbaIsPassed_WithPbaInErrorMessage() {
        Set<String> newPaymentAccounts = new HashSet<>();
        newPaymentAccounts.add("this-is-invalid");

        PbaEditRequest pbaEditRequest = PbaEditRequest.anPbaEditRequest().build();
        pbaEditRequest.setPaymentAccounts(newPaymentAccounts);

        String orgId = createActiveOrganisationAndPbaEditRequest();

        Map<String, Object> response = professionalReferenceDataClient.editPaymentsAccountsByOrgId(pbaEditRequest, orgId, hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body").toString()).contains("PBA numbers must start with PBA/pba and be followed by 7 alphanumeric characters. The following PBAs entered are invalid: this-is-invalid");
    }

    private String createActiveOrganisationAndPbaEditRequest() {
        Set<String> existingPaymentAccounts = new HashSet<>();
        existingPaymentAccounts.add("PBA0000001");
        existingPaymentAccounts.add("PBA0000002");

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().paymentAccount(existingPaymentAccounts).build();
        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgId = (String) responseForOrganisationCreation.get("organisationIdentifier");
        updateOrganisation(orgId, hmctsAdmin, ACTIVE);

        return orgId;
    }
}
