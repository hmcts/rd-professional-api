package uk.gov.hmcts.reform.professionalapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsWithPbaStatusResponse;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

@SuppressWarnings("unchecked")
class FindOrganisationsByPbaStatusTest extends AuthorizationEnabledIntegrationTest {

    @BeforeEach
    void setUpTestData() {
        setUpTestDataForPba();
    }

    @Test
    void get_request_returns_organisations_with_accepted_pba_status() throws JsonProcessingException {
        var orgPbaResponse = (List<OrganisationsWithPbaStatusResponse>)
                professionalReferenceDataClient.findOrganisationsByPbaStatus(
                        PbaStatus.ACCEPTED.toString(), hmctsAdmin, Boolean.FALSE);

        validatePbaResponse(orgPbaResponse, 2, PbaStatus.ACCEPTED);
    }

    @Test
    void get_request_returns_organisations_with_pending_pba_status() throws JsonProcessingException {
        var orgPbaResponse = (List<OrganisationsWithPbaStatusResponse>)
                professionalReferenceDataClient.findOrganisationsByPbaStatus(
                        PbaStatus.PENDING.toString(), hmctsAdmin, Boolean.FALSE);

        validatePbaResponse(orgPbaResponse, 1, PbaStatus.PENDING);
    }


    @Test
    void get_request_returns_no_organisations_for_given_pba_status() throws JsonProcessingException {
        var orgPbaResponse = (List<OrganisationsWithPbaStatusResponse>)
                professionalReferenceDataClient.findOrganisationsByPbaStatus(
                        PbaStatus.REJECTED.toString(), hmctsAdmin, Boolean.FALSE);

        assertThat(orgPbaResponse).isEmpty();
    }

    @Test
    void get_request_success_when_pba_status_lower_case() throws JsonProcessingException {
        var orgPbaResponse = (List<OrganisationsWithPbaStatusResponse>) professionalReferenceDataClient
                .findOrganisationsByPbaStatus("accepted", hmctsAdmin, Boolean.FALSE);

        validatePbaResponse(orgPbaResponse, 2, PbaStatus.ACCEPTED);
    }

    @Test
    void get_request_success_when_pba_status_upper_case() throws JsonProcessingException {
        var orgPbaResponse = (List<OrganisationsWithPbaStatusResponse>) professionalReferenceDataClient
                .findOrganisationsByPbaStatus("ACCEPTED", hmctsAdmin, Boolean.FALSE);

        validatePbaResponse(orgPbaResponse, 2, PbaStatus.ACCEPTED);
    }

    @Test
    void get_request_returns_400_when_empty_pba_status() throws JsonProcessingException {
        Map<String, Object> errorResponseMap = (Map<String, Object>) professionalReferenceDataClient
                .findOrganisationsByPbaStatus("\"\"", hmctsAdmin, Boolean.FALSE);

        validateInvalidRequestErrorResponse(errorResponseMap);
    }

    @Test
    void get_request_returns_400_when_pba_status_with_space() throws JsonProcessingException {
        Map<String, Object> errorResponseMap = (Map<String, Object>) professionalReferenceDataClient
                .findOrganisationsByPbaStatus("\" \"", hmctsAdmin, Boolean.FALSE);

        validateInvalidRequestErrorResponse(errorResponseMap);
    }

    @Test
    void get_request_returns_400_when_invalid_pba_status() throws JsonProcessingException {
        Map<String, Object> invalidStatusErrorResponseMap = (Map<String, Object>) professionalReferenceDataClient
                .findOrganisationsByPbaStatus("Invalid Status", hmctsAdmin, Boolean.FALSE);
        Map<String, Object> nullStatusErrorResponseMap = (Map<String, Object>) professionalReferenceDataClient
                .findOrganisationsByPbaStatus(null, hmctsAdmin, Boolean.FALSE);
        Map<String, Object> specialCharErrorResponseMap = (Map<String, Object>) professionalReferenceDataClient
                .findOrganisationsByPbaStatus("*", hmctsAdmin, Boolean.FALSE);

        validateInvalidRequestErrorResponse(invalidStatusErrorResponseMap);
        validateInvalidRequestErrorResponse(nullStatusErrorResponseMap);
        validateInvalidRequestErrorResponse(specialCharErrorResponseMap);
    }

    @Test
    void get_request_returns_400_when_multiple_pba_status() throws JsonProcessingException {

        Map<String, Object> errorResponseMap = (Map<String, Object>) professionalReferenceDataClient
                .findOrganisationsByPbaStatus(PbaStatus.ACCEPTED + "," + PbaStatus.PENDING, hmctsAdmin,
                        Boolean.FALSE);

        validateInvalidRequestErrorResponse(errorResponseMap);
    }

    @Test
    void get_request_returns_403_when_invalid_role() throws JsonProcessingException {
        Map<String, Object> errorResponseMap = (Map<String, Object>) professionalReferenceDataClient
                .findOrganisationsByPbaStatus(PbaStatus.ACCEPTED.toString(), "prd-role", Boolean.FALSE);
        ErrorResponse errorResponse = (ErrorResponse) errorResponseMap.get("response_body");

        assertThat(errorResponseMap).isNotNull().containsEntry("http_status", HttpStatus.FORBIDDEN);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(ErrorConstants.ACCESS_EXCEPTION.getErrorMessage());
    }

    @Test
    void get_request_returns_401_when_unauthorised() throws JsonProcessingException {
        Map<String, Object> errorResponseMap = (Map<String, Object>) professionalReferenceDataClient
                .findOrganisationsByPbaStatus(PbaStatus.ACCEPTED.toString(), hmctsAdmin, Boolean.TRUE);

        assertThat(errorResponseMap).isNotNull().containsEntry("http_status", HttpStatus.UNAUTHORIZED);
    }

    public void setUpTestDataForPba() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        createActiveOrganisation1();
        createActiveOrganisation2();
        createPendingOrganisation();
    }

    public void createActiveOrganisation1() {
        Set<String> paymentAccountsOrg1 = new HashSet<>();
        paymentAccountsOrg1.add("PBA1234568");
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String orgName1 = randomAlphabetic(7);
        createAndActivateOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().name(orgName1).sraId(randomAlphabetic(10))
                        .paymentAccount(paymentAccountsOrg1).build());
    }

    public void createActiveOrganisation2() {
        Set<String> paymentAccountsOrg2 =  new HashSet<>();
        paymentAccountsOrg2.add("PBA1234570");
        paymentAccountsOrg2.add("PBA1234569");
        String orgName2 = randomAlphabetic(7);
        createAndActivateOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().name(orgName2).sraId(randomAlphabetic(10))
                        .paymentAccount(paymentAccountsOrg2).build());
    }

    public void createPendingOrganisation() {
        Set<String> paymentAccountsOrg3 =  new HashSet<>();
        paymentAccountsOrg3.add("PBA1234571");
        paymentAccountsOrg3.add("PBA1234572");
        String orgName3 = randomAlphabetic(7);
        createOrganisationWithGivenRequest(
                someMinimalOrganisationRequest().name(orgName3).sraId(randomAlphabetic(10))
                        .paymentAccount(paymentAccountsOrg3).build());
    }

    private void validatePbaResponse(List<OrganisationsWithPbaStatusResponse> orgPbaResponse, int expectedOrgsize,
                                     PbaStatus expectedPbaStatus) {
        assertThat(orgPbaResponse).isNotNull().hasSize(expectedOrgsize);
        orgPbaResponse.forEach(org -> assertNotNull((org.getPbaNumbers())));
        orgPbaResponse.forEach(org -> assertTrue(org.getPbaNumbers().stream()
                .allMatch(pba -> pba.getStatus().equalsIgnoreCase(expectedPbaStatus.toString()))));
        if (expectedPbaStatus.equals(PbaStatus.ACCEPTED)) {
            orgPbaResponse.forEach(org -> org.getPbaNumbers().forEach(pba -> assertNotNull(pba.getDateAccepted())));
        } else if (expectedPbaStatus.equals(PbaStatus.PENDING)) {
            orgPbaResponse.forEach(org -> org.getPbaNumbers().forEach(pba -> assertNull(pba.getDateAccepted())));
        }
    }

    private void validateInvalidRequestErrorResponse(Map<String, Object> errorResponseMap) {
        ErrorResponse errorResponse = (ErrorResponse) errorResponseMap.get("response_body");
        assertThat(errorResponseMap).isNotNull().containsEntry("http_status", HttpStatus.BAD_REQUEST);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(ErrorConstants.INVALID_REQUEST.getErrorMessage());
    }

}
