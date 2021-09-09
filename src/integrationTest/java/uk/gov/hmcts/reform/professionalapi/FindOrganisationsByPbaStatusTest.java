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
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;

@SuppressWarnings("unchecked")
public class FindOrganisationsByPbaStatusTest extends AuthorizationEnabledIntegrationTest {

    @Test
    public void get_request_returns_organisations_with_correct_pba_status() throws JsonProcessingException {
        createOrganizationsAndEditPbas();

        List<OrganisationsWithPbaStatusResponse> orgPbaResponse = (List<OrganisationsWithPbaStatusResponse>)
                professionalReferenceDataClient.findOrganisationsByPbaStatus(
                        PbaStatus.ACCEPTED.toString(), hmctsAdmin, Boolean.FALSE);

        assertThat(orgPbaResponse).isNotNull();
    }

    @Test
    public void get_request_returns_no_organisations_for_given_pba_status() throws JsonProcessingException {
        createOrganizationsAndEditPbas();

        List<OrganisationsWithPbaStatusResponse> orgPbaResponse = (List<OrganisationsWithPbaStatusResponse>)
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
    public void get_request_returns_403_when_invalid_role() throws JsonProcessingException {
        createOrganizationsAndEditPbas();

        Map<String, Object> errorResponseMap = (Map<String, Object>) professionalReferenceDataClient
                .findOrganisationsByPbaStatus(PbaStatus.ACCEPTED.toString(), "invalid role", Boolean.FALSE);
        ErrorResponse errorResponse = (ErrorResponse) errorResponseMap.get("response_body");

        assertThat(errorResponseMap).isNotNull().containsEntry("http_status", HttpStatus.FORBIDDEN);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(ErrorConstants.ACCESS_EXCEPTION.getErrorMessage());
    }

    @Test
    public void get_request_returns_401_when_invalid_role() throws JsonProcessingException {
        Map<String, Object> errorResponseMap = (Map<String, Object>) professionalReferenceDataClient
                .findOrganisationsByPbaStatus(PbaStatus.ACCEPTED.toString(), hmctsAdmin, Boolean.TRUE);

        assertThat(errorResponseMap).isNotNull().containsEntry("http_status", HttpStatus.UNAUTHORIZED);
    }


    private void createOrganizationsAndEditPbas() {
        Set<String> paymentAccountsOrg1 = new HashSet<>();
        paymentAccountsOrg1.add("PBA1234568");

        Set<String> paymentAccountsOrg2 =  new HashSet<>();
        paymentAccountsOrg2.add("PBA1234568");
        paymentAccountsOrg2.add("PBA1234569");

        OrganisationCreationRequest organisationCreationRequest1 = organisationRequestWithAllFields()
                .paymentAccount(paymentAccountsOrg1).build();

        OrganisationCreationRequest organisationCreationRequest2 = organisationRequestWithAllFields()
                .paymentAccount(paymentAccountsOrg2).build();

        String organisationIdentifier1 = (String) professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest1).get(ORG_IDENTIFIER);

        String organisationIdentifier2 = (String) professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest2).get(ORG_IDENTIFIER);

        updateOrganisation(organisationIdentifier1, hmctsAdmin, "ACTIVE");
        updateOrganisation(organisationIdentifier2, hmctsAdmin, "ACTIVE");
    }


}
