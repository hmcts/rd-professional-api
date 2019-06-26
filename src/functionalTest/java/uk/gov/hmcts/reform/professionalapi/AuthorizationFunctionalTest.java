package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient;
import uk.gov.hmcts.reform.professionalapi.client.S2sClient;
import uk.gov.hmcts.reform.professionalapi.config.Oauth2;
import uk.gov.hmcts.reform.professionalapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.professionalapi.idam.IdamService;


@RunWith(SpringIntegrationSerenityRunner.class)
@ContextConfiguration(classes = {TestConfigProperties.class, Oauth2.class})
@ComponentScan("uk.gov.hmcts.reform.professionalapi")
@TestPropertySource("classpath:application-functional.yaml")
@Slf4j
public abstract class AuthorizationFunctionalTest {

    @Value("${s2s-url}")
    protected String s2sUrl;

    @Value("${s2s-name}")
    protected String s2sName;

    @Value("${s2s-secret}")
    protected String s2sSecret;

    @Value("${targetInstance}")
    protected String professionalApiUrl;

    @Value("${exui.role.hmcts-admin}")
    protected String hmctsAdmin;

    @Value("${exui.role.pui-user-manager}")
    protected String puiUserManager;

    @Value("${exui.role.pui-organisation-manager}")
    protected String puiOrgManager;

    @Value("${exui.role.pui-finance-manager}")
    protected String puiFinanceManager;

    @Value("${exui.role.pui-case-manager}")
    protected String puiCaseManager;
    

    protected ProfessionalApiClient professionalApiClient;

    protected IdamService idamService;

    @Autowired
    protected TestConfigProperties configProperties;


    @Before
    public void setUp() {
        log.info("Configured S2S secret: " + s2sSecret.substring(0, 2) + "************" + s2sSecret.substring(14));
        log.info("Configured S2S microservice: " + s2sName);
        log.info("Configured S2S URL: " + s2sUrl);


        String s2sToken = new S2sClient(s2sUrl, s2sName, s2sSecret).signIntoS2S();

        idamService = new IdamService(configProperties);

        professionalApiClient = new ProfessionalApiClient(
                                                          professionalApiUrl,
                                                          s2sToken, idamService);
    }


    protected String getUserAccessToken(String role) {

        idamService = new IdamService(configProperties);
        return idamService.createUserWith("", role).getAuthorisationToken();
    }

    @After
    public void tearDown() {
    }

    protected String createAndUpdateOrganisationToActive() {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String organisationIdentifier = (String) response.get("organisationIdentifier");
        assertThat(organisationIdentifier).isNotEmpty();
        professionalApiClient.updateOrganisation(organisationIdentifier,puiCaseManager);
        return organisationIdentifier;

    }

    protected void validateUsers(Map<String, Object> searchResponse) {
        assertThat(searchResponse.get("users")).asList().isNotEmpty();

        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");
        HashMap professionalUsersResponse = professionalUsersResponses.get(0);

        assertThat(professionalUsersResponse.get("userIdentifier")).isNotNull();
        assertThat(professionalUsersResponse.get("firstName")).isNotNull();
        assertThat(professionalUsersResponse.get("lastName")).isNotNull();
        assertThat(professionalUsersResponse.get("email")).isNotNull();
        assertThat(professionalUsersResponse.get("status")).isNotNull();
        assertThat(((List)professionalUsersResponse.get("roles")).size()).isEqualTo(0);
    }
}
