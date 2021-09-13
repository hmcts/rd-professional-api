package uk.gov.hmcts.reform.professionalapi;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsWithPbaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;

@SuppressWarnings("unchecked")
public class FindOrganisationsByPbaStatusTest extends AuthorizationEnabledIntegrationTest {

    @Test
    public void get_request_returns_organisations_with_accepted_pba_status() throws JsonProcessingException {
        createOrganizationsAndEditPbas();
        var orgPbaResponse = (List<OrganisationsWithPbaStatusResponse>)
                professionalReferenceDataClient.findOrganisationsByPbaStatus(
                        PbaStatus.ACCEPTED.toString(), hmctsAdmin, Boolean.FALSE);

        assertThat(orgPbaResponse).isNotNull();
        assertThat(orgPbaResponse.get(0).getPbaNumbers().size()).isEqualTo(1);
        assertThat(orgPbaResponse.get(0).getPbaNumbers().get(0).getStatus()).isEqualTo(ACCEPTED.toString());
        assertThat(orgPbaResponse.get(0).getPbaNumbers().get(0).getPbaNumber()).isEqualTo("PBA1234568");
        assertThat(orgPbaResponse.get(0).getPbaNumbers().get(0).getDateAccepted()).isNotNull();
    }

    @Test
    public void get_request_returns_organisations_with_pending_pba_status() throws JsonProcessingException {
        createOrganizationsPending();

        var orgPbaResponse = (List<OrganisationsWithPbaStatusResponse>)
                professionalReferenceDataClient.findOrganisationsByPbaStatus(
                        PbaStatus.PENDING.toString(), hmctsAdmin, Boolean.FALSE);

        assertThat(orgPbaResponse).isNotNull();
        assertThat(orgPbaResponse.get(0).getPbaNumbers().size()).isEqualTo(1);
        assertThat(orgPbaResponse.get(0).getPbaNumbers().get(0).getStatus()).isEqualTo(PbaStatus.PENDING.toString());
        assertThat(orgPbaResponse.get(0).getPbaNumbers().get(0).getPbaNumber()).isEqualTo("PBA1234569");
        assertThat(orgPbaResponse.get(0).getPbaNumbers().get(0).getDateAccepted()).isNull();
    }

    @Test
    public void get_request_returns_no_organisations_for_pending_org_with_accepted_pba_status()
            throws JsonProcessingException {
        createOrganizationsPending();

        var orgPbaResponse = (List<OrganisationsWithPbaStatusResponse>)
                professionalReferenceDataClient.findOrganisationsByPbaStatus(
                        PbaStatus.ACCEPTED.toString(), hmctsAdmin, Boolean.FALSE);

        assertThat(orgPbaResponse).isEmpty();
    }


    @Test
    public void get_request_returns_no_organisations_for_given_pba_status() throws JsonProcessingException {
        createOrganizationsAndEditPbas();

        var orgPbaResponse = (List<OrganisationsWithPbaStatusResponse>)
                professionalReferenceDataClient.findOrganisationsByPbaStatus(
                        PbaStatus.REJECTED.toString(), hmctsAdmin, Boolean.FALSE);

        assertThat(orgPbaResponse).isEmpty();
    }

    @Test
    public void get_request_returns_400_when_invalid_pba_status() throws JsonProcessingException {
        createOrganizationsAndEditPbas();

        Map<String, Object> errorResponseMap = (Map<String, Object>) professionalReferenceDataClient
                .findOrganisationsByPbaStatus("Invalid Status", hmctsAdmin, Boolean.FALSE);
        ErrorResponse errorResponse = (ErrorResponse) errorResponseMap.get("response_body");

        assertThat(errorResponseMap).isNotNull().containsEntry("http_status", HttpStatus.BAD_REQUEST);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(ErrorConstants.INVALID_REQUEST.getErrorMessage());
    }

    @Test
    public void get_request_returns_400_when_null_pba_status() throws JsonProcessingException {
        createOrganizationsAndEditPbas();

        Map<String, Object> errorResponseMap = (Map<String, Object>) professionalReferenceDataClient
                .findOrganisationsByPbaStatus(null, hmctsAdmin, Boolean.FALSE);
        ErrorResponse errorResponse = (ErrorResponse) errorResponseMap.get("response_body");

        assertThat(errorResponseMap).isNotNull().containsEntry("http_status", HttpStatus.BAD_REQUEST);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(ErrorConstants.INVALID_REQUEST.getErrorMessage());
    }

    @Test
    public void get_request_returns_403_when_unauthorised_role() throws JsonProcessingException {
        createOrganizationsAndEditPbas();

        Map<String, Object> errorResponseMap = (Map<String, Object>) professionalReferenceDataClient
                .findOrganisationsByPbaStatus(ACCEPTED.toString(), "unauthorised role", Boolean.FALSE);
        ErrorResponse errorResponse = (ErrorResponse) errorResponseMap.get("response_body");

        assertThat(errorResponseMap).isNotNull().containsEntry("http_status", HttpStatus.FORBIDDEN);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(ErrorConstants.ACCESS_EXCEPTION.getErrorMessage());
    }

    @Test
    public void get_request_returns_401_when_invalid_role() throws JsonProcessingException {
        Map<String, Object> errorResponseMap = (Map<String, Object>) professionalReferenceDataClient
                .findOrganisationsByPbaStatus(ACCEPTED.toString(), hmctsAdmin, Boolean.TRUE);

        assertThat(errorResponseMap).isNotNull().containsEntry("http_status", HttpStatus.UNAUTHORIZED);
    }


    private void createOrganizationsAndEditPbas() {
        Set<String> paymentAccountsOrg = new HashSet<>();
        paymentAccountsOrg.add("PBA1234568");

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields()
                .paymentAccount(paymentAccountsOrg).build();

        String organisationIdentifier = (String) professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest).get(ORG_IDENTIFIER);

        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE", organisationCreationRequest);
    }

    private void createOrganizationsPending() {
        Set<String> paymentAccountsOrg = new HashSet<>();
        paymentAccountsOrg.add("PBA1234569");

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields()
                .paymentAccount(paymentAccountsOrg).build();

        professionalReferenceDataClient.createOrganisation(organisationCreationRequest).get(ORG_IDENTIFIER);
    }

}
