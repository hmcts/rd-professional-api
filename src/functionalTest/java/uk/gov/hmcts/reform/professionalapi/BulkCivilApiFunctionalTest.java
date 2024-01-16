package uk.gov.hmcts.reform.professionalapi;

import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.lib.client.response.S2sClient;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;
import uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient;
import uk.gov.hmcts.reform.professionalapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.professionalapi.controller.request.BulkCustomerRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.BulkCustomerDetails;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;
import uk.gov.hmcts.reform.professionalapi.repository.BulkCustomerDetailsRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.professionalapi.util.ToggleEnable;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SerenityTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource({"classpath:application.yaml","classpath:application-functional-bulkcustomer.yaml"})
public class BulkCivilApiFunctionalTest {
    @Autowired
    BulkCustomerDetailsRepository bulkCustomerDetailsRepository;

    @Autowired
    PaymentAccountRepository paymentAccountRepository;
    @Value("${s2s-secret}")
    protected String s2sSecret;

    @Value("${s2s-url}")
    protected String s2sUrl;

    @Value("${s2s-name}")
    protected String s2sName;


    protected static ProfessionalApiClient professionalApiClient;

    private static final String SERVICE_HEADER = "ServiceAuthorization";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    OrganisationOtherOrgsCreationRequest organisationOtherOrgsCreationRequest;

    @Value("${prd.security.roles.hmcts-admin}")
    protected String hmctsAdmin;

    @LocalServerPort
    private int port;


    private String baseUrl = "http://localhost";

    protected static String  s2sToken;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    protected TestConfigProperties configProperties;

    protected static IdamOpenIdClient idamOpenIdClient;


    @Value("${targetInstance}")
    protected String professionalApiUrl;


    @BeforeEach
    public  void init() {

        baseUrl = baseUrl.concat(":").concat(port + "");
        SerenityRest.useRelaxedHTTPSValidation();
        SerenityRest.setDefaultParser(Parser.JSON);


        if (null == s2sToken) {
            s2sToken = new S2sClient(s2sUrl, s2sName, s2sSecret).signIntoS2S();
        }

        if (null == idamOpenIdClient) {
            idamOpenIdClient = new IdamOpenIdClient(configProperties);
        }
        professionalApiClient = new ProfessionalApiClient(
                baseUrl,
                s2sToken, idamOpenIdClient);
    }

    @Test
    @DisplayName("PRD Bulk Customer API Test Scenarios")
    void testBulkCustomerApiScenarios() {
        retrieveBulkCustomerDetailsSuccess();
        retrieveBulkCustomerDetailsWithoutPaymentAccountSuccess();
        retrieveBulkCustomerDetailsWithoutActiveOrganisation();
        retrieveBulkCustomerDetailsWithNoDetailsInDb();
        retrieveBulkCustomerDetailsWithInvalidRole();
        retrieveBulkCustomerDetailsWithInvalidBulkCustomerID();
        retrieveBulkCustomerDetailsWithInvalidSidamID();
        retrieveBulkCustomerDetailsWithEmptySidamID();
        retrieveBulkCustomerDetailsWithEmptyBulkCustomerID();

    }


    void retrieveBulkCustomerDetailsSuccess() {

        setUpDataForBulkCustomer(OrganisationStatus.ACTIVE,"bulkCustomerId1","sidamId1",
                "PBA1234567",  "W98ZZ01", "Test Org");

        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("bulkCustomerId1");
        bulkCustomerRequest.setIdamId("sidamId1");

        Map<String, Object> bulkOrganisationResponse = retrieveOrganisationForBulkCustomerDetails(bulkCustomerRequest,
                HttpStatus.OK, "caseworker-civil-admin");
        assertThat(bulkOrganisationResponse.get("organisationId")).isEqualTo("W98ZZ01");
        assertThat(bulkOrganisationResponse.get("organisationName")).isEqualTo("Test Org");
        assertThat(bulkOrganisationResponse.get("paymentAccount")).isEqualTo("PBA1234567");


    }



    void retrieveBulkCustomerDetailsWithoutPaymentAccountSuccess() {

        setUpDataForBulkCustomer(OrganisationStatus.ACTIVE,"bulkCustomerId2","sidamId2",
                "", "W98ZZ02", "Test Org");


        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("bulkCustomerId2");
        bulkCustomerRequest.setIdamId("sidamId2");


        Map<String, Object> bulkOrganisationResponse = retrieveOrganisationForBulkCustomerDetails(bulkCustomerRequest,
                HttpStatus.OK,"caseworker-civil-admin");
        assertThat(bulkOrganisationResponse.get("organisationId")).isEqualTo("W98ZZ02");
        assertThat(bulkOrganisationResponse.get("organisationName")).isEqualTo("Test Org");
        assertThat(bulkOrganisationResponse.get("paymentAccount")).isEqualTo("");


    }


    void retrieveBulkCustomerDetailsWithoutActiveOrganisation() {

        setUpDataForBulkCustomer(OrganisationStatus.PENDING,"bulkCustomerId3","sidamId3",
               "PBA1234569", "W98ZZ03", "Test Org");


        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("bulkCustomerId3");
        bulkCustomerRequest.setIdamId("sidamId3");

        Map<String, Object> bulkOrganisationResponse = retrieveOrganisationForBulkCustomerDetails(bulkCustomerRequest,
                HttpStatus.NOT_FOUND,"caseworker-civil-admin");
        assertThat(bulkOrganisationResponse.get("errorDescription").toString())
                .contains("Record not found");


    }


    void retrieveBulkCustomerDetailsWithNoDetailsInDb() {


        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("bulkCustomerIdtest");
        bulkCustomerRequest.setIdamId("sidamIdtest");

        Map<String, Object> bulkOrganisationResponse = retrieveOrganisationForBulkCustomerDetails(bulkCustomerRequest,
                HttpStatus.NOT_FOUND,"caseworker-civil-admin");
        assertThat(bulkOrganisationResponse.get("errorDescription").toString())
                .contains("Record not found");
    }



    void retrieveBulkCustomerDetailsWithInvalidRole() {


        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("bulkCustomerId");
        bulkCustomerRequest.setIdamId("sidamId");

        Map<String, Object> bulkOrganisationResponse = retrieveOrganisationForBulkCustomerDetails(bulkCustomerRequest,
                HttpStatus.FORBIDDEN, "pui-user-manager");
        assertThat(bulkOrganisationResponse.get("errorDescription").toString())
                .contains("Access is denied");


    }



    void retrieveBulkCustomerDetailsWithInvalidBulkCustomerID() {


        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("bulkCust&*(omerId");
        bulkCustomerRequest.setIdamId("sidamId");

        Map<String, Object> bulkOrganisationResponse = retrieveOrganisationForBulkCustomerDetails(bulkCustomerRequest,
                HttpStatus.BAD_REQUEST,  "caseworker-civil-admin");
        assertThat(bulkOrganisationResponse.get("errorMessage").toString())
                .contains("There is a problem with your request. Please check and try again");

    }


    void retrieveBulkCustomerDetailsWithInvalidSidamID() {


        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("bulkCustomerId");
        bulkCustomerRequest.setIdamId("sidam*£Id");

        Map<String, Object> bulkOrganisationResponse = retrieveOrganisationForBulkCustomerDetails(bulkCustomerRequest,
                HttpStatus.BAD_REQUEST,  "caseworker-civil-admin");
        assertThat(bulkOrganisationResponse.get("errorMessage").toString())
                .contains("There is a problem with your request. Please check and try again");
    }


    void retrieveBulkCustomerDetailsWithEmptySidamID() {


        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("bulkCustomerId");
        bulkCustomerRequest.setIdamId("");

        Map<String, Object> bulkOrganisationResponse = retrieveOrganisationForBulkCustomerDetails(bulkCustomerRequest,
                HttpStatus.BAD_REQUEST,  "caseworker-civil-admin");
        assertThat(bulkOrganisationResponse.get("errorMessage").toString())
                .contains("There is a problem with your request. Please check and try again");
    }

    void retrieveBulkCustomerDetailsWithEmptyBulkCustomerID() {


        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("");
        bulkCustomerRequest.setIdamId("sidamId");

        Map<String, Object> bulkOrganisationResponse = retrieveOrganisationForBulkCustomerDetails(bulkCustomerRequest,
                HttpStatus.BAD_REQUEST,  "caseworker-civil-admin");
        assertThat(bulkOrganisationResponse.get("errorMessage").toString())
                .contains("There is a problem with your request. Please check and try again");
    }

    @Test
    @ToggleEnable(mapKey = "BulkCustomerDetailsInternalController.retrieveOrganisationDetailsForBulkCustomer",
            withFeature = false)
    @ExtendWith(FeatureToggleConditionExtension.class)
    void retrieveBulkCustomerDetailsWithLaunchDarklyFlagOff() {
        BulkCustomerRequest bulkCustomerRequest = new BulkCustomerRequest();
        bulkCustomerRequest.setBulkCustomerId("bulkCustomerId");
        bulkCustomerRequest.setIdamId("sidamId");

        Map<String, Object> bulkOrganisationResponse = retrieveOrganisationForBulkCustomerDetails(bulkCustomerRequest,
                HttpStatus.FORBIDDEN,  "caseworker-civil-admin");
    }


    public Map<String, Object> retrieveOrganisationForBulkCustomerDetails(BulkCustomerRequest request,
                                                                          HttpStatus status, String role) {
        Response response = getMultipleAuthHeadersInternal(role)
                .body(request)
                .post("/refdata/internal/v1/bulkCustomer/")
                .andReturn();

        response.then()
                .assertThat()
                .statusCode(status.value());


        return response.as(Map.class);

    }

    private RequestSpecification getMultipleAuthHeadersInternal(String role) {
        return getMultipleAuthHeaders(idamOpenIdClient.getcwdAdminOpenIdToken(role));
    }

    public RequestSpecification getMultipleAuthHeaders(String userToken) {
        return SerenityRest.with()
                .relaxedHTTPSValidation()
                .baseUri(baseUrl)
                .header("Content-Type", APPLICATION_JSON_VALUE)
                .header("Accepts", APPLICATION_JSON_VALUE)
                .header(SERVICE_HEADER, "Bearer " + s2sToken)
                .header(AUTHORIZATION_HEADER, "Bearer " + userToken);
    }

    private void setUpDataForBulkCustomer(OrganisationStatus orgStatus, String bulkCustomerId, String sidamId,
                                          String pbaNumber, String organisationId, String orgName) {


        Organisation org = new Organisation();
        org.setName(orgName);
        org.setStatus(orgStatus);
        org.setSraRegulated(false);
        org.setOrganisationIdentifier(organisationId);
        organisationRepository.save(org);
        Organisation organisation = organisationRepository.findByOrganisationIdentifier(organisationId);

        PaymentAccount pba = new PaymentAccount();
        pba.setOrganisation(organisation);
        pba.setPbaStatus(PbaStatus.ACCEPTED);
        pba.setPbaNumber(pbaNumber);
        if (isNotEmpty(pbaNumber)) {
            paymentAccountRepository.save(pba);
        }


        BulkCustomerDetails bulkCustomerDetails = new BulkCustomerDetails();
        bulkCustomerDetails.setOrganisationId(organisationId);
        bulkCustomerDetails.setBulkCustomerId(bulkCustomerId);
        bulkCustomerDetails.setSidamId(sidamId);
        bulkCustomerDetails.setPbaNumber(pbaNumber);
        bulkCustomerDetailsRepository.save(bulkCustomerDetails);


    }


}
