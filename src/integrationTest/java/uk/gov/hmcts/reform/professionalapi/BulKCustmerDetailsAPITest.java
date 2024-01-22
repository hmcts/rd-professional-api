package uk.gov.hmcts.reform.professionalapi;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.BulkCustomerRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.BulkCustomerDetails;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.util.FeatureConditionEvaluation.FORBIDDEN_EXCEPTION_LD;

@Slf4j
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class BulKCustmerDetailsAPITest extends AuthorizationEnabledIntegrationTest {


    @Test
    void retrieveBulkCustomerDetailsSuccess() {

        final String orgIdentifier = setUpdataForBulkCustomer(ACTIVE, "bulkCustomerId", "sidamId",
                "PBA1234567");

        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("bulkCustomerId");
        bulkCustomerRequest.setIdamId("sidamId");


        Map<String, Object> bulkOrganisationResponse =
                professionalReferenceDataClient.retrieveBulkOrganisation(bulkCustomerRequest,
                        "caseworker-civil-admin");
        assertThat(bulkOrganisationResponse.get("http_status")).isEqualTo("200 OK");
        assertThat(bulkOrganisationResponse.get("organisationId")).isEqualTo(orgIdentifier);
        assertThat(bulkOrganisationResponse.get("organisationName")).isEqualTo("some-org-name");
        assertThat(bulkOrganisationResponse.get("paymentAccount")).isEqualTo("PBA1234567");


    }

    @Test
    void retrieveBulkCustomerDetailsWithoutPaymentAccountSuccess() {

        final String orgIdentifier = setUpdataForBulkCustomer(ACTIVE, "bulkCustomerId",
                "sidamId",
                "PBAjh1234567");


        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("bulkCustomerId");
        bulkCustomerRequest.setIdamId("sidamId");


        Map<String, Object> bulkOrganisationResponse =
                professionalReferenceDataClient.retrieveBulkOrganisation(bulkCustomerRequest,
                        "caseworker-civil-admin");
        assertThat(bulkOrganisationResponse.get("http_status")).isEqualTo("200 OK");
        assertThat(bulkOrganisationResponse.get("organisationId")).isEqualTo(orgIdentifier);
        assertThat(bulkOrganisationResponse.get("organisationName")).isEqualTo("some-org-name");
        assertThat(bulkOrganisationResponse.get("paymentAccount")).isEqualTo("");


    }

    @Test
    void retrieveBulkCustomerDetailsWithoutActiveOrganisation() {

        String orgIdentifier = setUpdataForBulkCustomer("PENDING", "bulkCustomerId",
                "sidamId",
                "PBAjh1234567");


        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("bulkCustomerId");
        bulkCustomerRequest.setIdamId("sidamId");

        Map<String, Object> bulkOrganisationResponse =
                professionalReferenceDataClient.retrieveBulkOrganisation(bulkCustomerRequest,
                        "caseworker-civil-admin");
        assertThat(bulkOrganisationResponse).containsEntry("http_status", "404");
        assertThat(bulkOrganisationResponse.get("response_body").toString())
                .contains("Record not found");


    }

    @Test
    void retrieveBulkCustomerDetailsWithNoDetailsInDb() {


        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("bulkCustomerId");
        bulkCustomerRequest.setIdamId("sidamId");

        Map<String, Object> bulkOrganisationResponse =
                professionalReferenceDataClient.retrieveBulkOrganisation(bulkCustomerRequest,
                        "caseworker-civil-admin");
        assertThat(bulkOrganisationResponse).containsEntry("http_status", "404");
        assertThat(bulkOrganisationResponse.get("response_body").toString())
                .contains("Record not found");
    }


    @Test
    void retrieveBulkCustomerDetailsWithInvalidRole() {


        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("bulkCustomerId");
        bulkCustomerRequest.setIdamId("sidamId");

        Map<String, Object> bulkOrganisationResponse =
                professionalReferenceDataClient.retrieveBulkOrganisation(bulkCustomerRequest,
                        "pui-user-manager");
        assertThat(bulkOrganisationResponse).containsEntry("http_status", "403");
        assertThat(bulkOrganisationResponse.get("response_body").toString())
                .contains("Access is denied");


    }


    @Test
    void retrieveBulkCustomerDetailsWithInvalidBulkCustomerID() {


        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("bulkCust&*(omerId");
        bulkCustomerRequest.setIdamId("sidamId");

        Map<String, Object> bulkOrganisationResponse =
                professionalReferenceDataClient.retrieveBulkOrganisation(bulkCustomerRequest,
                        "caseworker-civil-admin");
        assertThat(bulkOrganisationResponse).containsEntry("http_status", "400");
        assertThat(bulkOrganisationResponse.get("response_body").toString())
                .contains("There is a problem with your request. Please check and try again");

    }

    @Test
    void retrieveBulkCustomerDetailsWithInvalidSidamID() {


        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("bulkCustomerId");
        bulkCustomerRequest.setIdamId("sidam*£Id");

        Map<String, Object> bulkOrganisationResponse =
                professionalReferenceDataClient.retrieveBulkOrganisation(bulkCustomerRequest,
                        "caseworker-civil-admin");
        assertThat(bulkOrganisationResponse).containsEntry("http_status", "400");
        assertThat(bulkOrganisationResponse.get("response_body").toString())
                .contains("There is a problem with your request. Please check and try again");
    }

    @Test
    void retrieveBulkCustomerDetailsWithEmptySidamID() {


        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("bulkCustomerId");
        bulkCustomerRequest.setIdamId("");

        Map<String, Object> bulkOrganisationResponse =
                professionalReferenceDataClient.retrieveBulkOrganisation(bulkCustomerRequest,
                        "caseworker-civil-admin");
        assertThat(bulkOrganisationResponse).containsEntry("http_status", "400");
        assertThat(bulkOrganisationResponse.get("response_body").toString())
                .contains("There is a problem with your request. Please check and try again");
    }

    @Test
    void retrieveBulkCustomerDetailsWithEmptyBulkCustomerID() {


        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("");
        bulkCustomerRequest.setIdamId("sidamId");

        Map<String, Object> bulkOrganisationResponse =
                professionalReferenceDataClient.retrieveBulkOrganisation(bulkCustomerRequest,
                        "caseworker-civil-admin");
        assertThat(bulkOrganisationResponse).containsEntry("http_status", "400");
        assertThat(bulkOrganisationResponse.get("response_body").toString())
                .contains("There is a problem with your request. Please check and try again");
    }

    @Test
    void retrieveBulkCustomerDetailsWithLaunchDarklyFlagOff() {
        Map<String, String> launchDarklyMap = new HashMap<>();
        launchDarklyMap.put("BulkCustomerDetailsInternalController.retrieveOrganisationDetailsForBulkCustomer",
                "test-bulk-api-flag");
        when(featureToggleService.isFlagEnabled(anyString(), anyString())).thenReturn(false);
        when(featureToggleService.getLaunchDarklyMap()).thenReturn(launchDarklyMap);


        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("bulkCustomerId");
        bulkCustomerRequest.setIdamId("sidamId");

        Map<String, Object> errorResponseMap =
                professionalReferenceDataClient.retrieveBulkOrganisation(bulkCustomerRequest,
                        "caseworker-civil-admin");

        assertThat(errorResponseMap).containsEntry("http_status", "403");
        assertThat((String) errorResponseMap.get("response_body"))
                .contains("test-bulk-api-flag".concat(SPACE).concat(FORBIDDEN_EXCEPTION_LD));
    }


    private String setUpdataForBulkCustomer(String orgStatus, String bulkCustomerId, String sidamId, String pba) {
        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgIdentifier = (String) response.get(ORG_IDENTIFIER);
        updateOrganisationWithGivenRequest(organisationCreationRequest, orgIdentifier, hmctsAdmin, orgStatus);
        BulkCustomerDetails bulkCustomerDetails = new BulkCustomerDetails();
        bulkCustomerDetails.setOrganisationId(orgIdentifier);
        bulkCustomerDetails.setBulkCustomerId(bulkCustomerId);
        bulkCustomerDetails.setSidamId(sidamId);
        bulkCustomerDetails.setPbaNumber(pba);
        bulkCustomerDetailsRepository.save(bulkCustomerDetails);
        return orgIdentifier;
    }


}
