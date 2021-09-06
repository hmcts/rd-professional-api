package uk.gov.hmcts.reform.professionalapi;


import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;

public class FindOrganisationsByPbaStatusTest extends AuthorizationEnabledIntegrationTest {

    @Test
    public void get_request_returns_organisations_with_correct_pba_status() {
        createOrganizationsAndEditPbas();

        Map<String, Object> orgPbaResponse = professionalReferenceDataClient
                .findOrganisationsByPbaStatus(PbaStatus.ACCEPTED.toString(), hmctsAdmin, Boolean.FALSE);

        assertThat(orgPbaResponse).isNotNull().containsEntry("http_status", "200 OK");
        assertThat(orgPbaResponse.get("orgWithPbas")).isNotNull();
    }

    @Test
    public void get_request_returns_no_organisations_for_given_pba_status() {
        createOrganizationsAndEditPbas();

        Map<String, Object> orgPbaResponse = professionalReferenceDataClient
                .findOrganisationsByPbaStatus(PbaStatus.REJECTED.toString(), hmctsAdmin, Boolean.FALSE);

        assertThat(orgPbaResponse).isNotNull().containsEntry("http_status", "200 OK");
        assertThat(orgPbaResponse.get("orgWithPbas")).isNull();
    }

    @Test
    public void get_request_returns_400_when_invalid_pba_status() {
        createOrganizationsAndEditPbas();

        Map<String, Object> orgPbaResponse = professionalReferenceDataClient
                .findOrganisationsByPbaStatus("Invalid Status", hmctsAdmin, Boolean.FALSE);

        assertThat(orgPbaResponse).isNotNull().containsEntry("http_status", "400");
        assertThat(orgPbaResponse.get("response_body").toString())
                .contains(ErrorConstants.INVALID_REQUEST.getErrorMessage());
    }

    @Test
    public void get_request_returns_403_when_invalid_role() {
        createOrganizationsAndEditPbas();

        Map<String, Object> orgPbaResponse = professionalReferenceDataClient
                .findOrganisationsByPbaStatus(PbaStatus.ACCEPTED.toString(), "invalid role", Boolean.FALSE);

        assertThat(orgPbaResponse).isNotNull().containsEntry("http_status", "403");
    }

    @Test
    public void get_request_returns_401_when_invalid_role() {
        createOrganizationsAndEditPbas();

        Map<String, Object> orgPbaResponse = professionalReferenceDataClient
                .findOrganisationsByPbaStatus(PbaStatus.ACCEPTED.toString(), hmctsAdmin, Boolean.TRUE);

        assertThat(orgPbaResponse).isNotNull().containsEntry("http_status", "401");
    }


    private void createOrganizationsAndEditPbas() {
        Set<String> paymentAccounts_org1 = new HashSet<>();
        paymentAccounts_org1.add("PBA1234568");

        Set<String> paymentAccounts_org2 =  new HashSet<>();
        paymentAccounts_org2.add("PBA1234568");
        paymentAccounts_org2.add("PBA1234569");

        OrganisationCreationRequest organisationCreationRequest1 = organisationRequestWithAllFields()
                .paymentAccount(paymentAccounts_org1).build();

        OrganisationCreationRequest organisationCreationRequest2 = organisationRequestWithAllFields()
                .paymentAccount(paymentAccounts_org2).build();

        String organisationIdentifier1 = (String) professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest1).get(ORG_IDENTIFIER);

        String organisationIdentifier2 = (String) professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest2).get(ORG_IDENTIFIER);

        updateOrganisation(organisationIdentifier1, hmctsAdmin, "ACTIVE");
        updateOrganisation(organisationIdentifier2, hmctsAdmin, "ACTIVE");

        PbaEditRequest pbaEditRequest1 = new PbaEditRequest();
        pbaEditRequest1.setPaymentAccounts(paymentAccounts_org1);

        PbaEditRequest pbaEditRequest2 = new PbaEditRequest();
        pbaEditRequest2.setPaymentAccounts(paymentAccounts_org2);

        professionalReferenceDataClient.editPaymentsAccountsByOrgId(pbaEditRequest1, organisationIdentifier1, hmctsAdmin);
        professionalReferenceDataClient.editPaymentsAccountsByOrgId(pbaEditRequest2, organisationIdentifier2, hmctsAdmin);
    }


}
