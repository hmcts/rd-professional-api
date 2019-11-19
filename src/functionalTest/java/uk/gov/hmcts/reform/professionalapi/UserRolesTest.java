package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class UserRolesTest extends AuthorizationFunctionalTest {

    private String orgIdentifier;

    @Test
    public void ac1_super_user_can_have_fpla_or_iac_roles() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        orgIdentifier = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);

        response = professionalApiClient.retrieveOrganisationDetails(orgIdentifier, puiUserManager);
        log.info("ORG DETAILS RESPONSE::::::::::" + response);
        List<String> userRoles = (List<String>) response.get("roles");
        log.info("USER ROLES::::::::::::" + userRoles);
        assertThat(userRoles).contains("caseworker-publiclaw", "caseworker-publiclaw-solicitor", "caseworker-ia-legalrep-solicitor");
    }
}
