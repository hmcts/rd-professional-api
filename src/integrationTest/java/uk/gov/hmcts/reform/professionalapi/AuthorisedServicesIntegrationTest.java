package uk.gov.hmcts.reform.professionalapi;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.professionalapi.util.ProfessionalReferenceDataClient;
import uk.gov.hmcts.reform.professionalapi.util.SpringBootIntegrationTest;

@TestPropertySource(properties = {"S2S_URL=http://127.0.0.1:9990"})
@RunWith(SpringIntegrationSerenityRunner.class)
public class AuthorisedServicesIntegrationTest extends SpringBootIntegrationTest {

    @Value("${oidc.issuer}")
    private String issuer;

    @Value("${oidc.expiration}")
    private long expiration;

    protected ProfessionalReferenceDataClient professionalReferenceDataClient;

    @ClassRule
    public static WireMockRule s2sService = new WireMockRule(wireMockConfig().port(9990));

    @Before
    public void setUpClient() {
        professionalReferenceDataClient = new ProfessionalReferenceDataClient(port, issuer, expiration);
    }

    @Test
    public void returns_201_when_authorised_service_attempts_create_organisation() {
        s2sAuthorisedCallMock();

        Map<String, Object> response = professionalReferenceDataClient.createOrganisation(someMinimalOrganisationRequest().build());

        String httpStatus = (String) response.get("http_status");
        assertThat(httpStatus).isEqualTo("201 CREATED");

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotNull();
        assertThat(orgIdentifierResponse.length()).isEqualTo(LENGTH_OF_ORGANISATION_IDENTIFIER);
        assertThat(orgIdentifierResponse.matches(ORGANISATION_IDENTIFIER_FORMAT_REGEX)).isTrue();
    }

    @Test
    public void returns_401_when_unauthorised_service_attempts_create_organisation() {
        s2sUnauthorisedCallMock();

        Map<String, Object> response = professionalReferenceDataClient.createOrganisation(someMinimalOrganisationRequest().build());

        String httpStatus = (String) response.get("http_status");
        assertThat(httpStatus).isEqualTo("401");
    }

    private void s2sAuthorisedCallMock() {
        s2sService.stubFor(get(urlEqualTo("/details"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getAnAuthorisedServiceName())));
    }

    private void s2sUnauthorisedCallMock() {
        s2sService.stubFor(get(urlEqualTo("/details"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withHeader("Content-Type", "application/json")
                        .withBody("unauthorised_service")));
    }

    private String getAnAuthorisedServiceName() {
        List<String> authorisedServices = Arrays.asList("rd_professional_api", "rd_user_profile_api", "xui_webapp", "finrem_payment_service", "fpl_case_service", "iac", "aac-manage-case-assignment");
        return authorisedServices.get(new Random().nextInt(authorisedServices.size()));
    }
}