package uk.gov.hmcts.reform.professionalapi.security;

import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.professionalapi.util.TestApplicationServer;

import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;
import static uk.gov.hmcts.reform.professionalapi.util.ProfessionalReferenceDataClient.getHttpHeaders;

public class BaseSecurityIntegrationTest extends AuthorizationEnabledIntegrationTest {

    protected static final String VALID_ISSUER_1 = "http://localhost:5062/o";
    protected static final String VALID_ISSUER_2 = "https://secondary-idam.platform.hmcts.net";
    protected static final String ROGUE_ISSUER = "https://rogue-issuer.com";
    protected static final String UPDATE_ORG_URL = "/refdata/internal/v1/organisations/%s";

    protected String organisationIdentifier = null;

    protected OrganisationCreationRequest organisationUpdateRequest =
            organisationRequestWithAllFieldsAreUpdated()
                    .status(OrganisationStatus.ACTIVE.name())
                    .build();

    @Autowired
    private TestApplicationServer testApplicationServer;

    @BeforeAll
    public static void setUp() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
    }

    @BeforeEach
    public void create() {
        organisationIdentifier = createOrganisationRequest();
    }

    @AfterEach
    public void delete() {
        professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, organisationIdentifier);
    }

    protected RequestSpecification jwtRequest(String issuer, boolean expired) {

        return SerenityRest.given()
                .baseUri(testApplicationServer.getBaseUrl())
                .headers(getHttpHeaders(issuer, expired, null, hmctsAdmin));

    }

    protected RequestSpecification unexpiredJwt(
            String issuer) {

        return jwtRequest(issuer, false);
    }

    protected RequestSpecification expiredJwt(
            String issuer)
            throws Exception {

        return jwtRequest(issuer, true);
    }
}
