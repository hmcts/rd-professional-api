package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
public class UpdateOrganisationTest extends AuthorizationFunctionalTest {

    @Test
    public void can_update_an_organisation() {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);
    }

    @Test
    public void can_update_an_organisation_with_unknown_jurisdiction_should_return_400() {

        Map<String, Object> response = professionalApiClient.createOrganisationWithUnknownJurisdictionId();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void can_update_an_organisation_with_no_jurisdiction_should_return_400() {

        Map<String, Object> response = professionalApiClient.createOrganisationWithNoJurisdictionId();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin, HttpStatus.BAD_REQUEST);
    }


    // This test is for validating if old implementation i.e. Bearer token still works along with OPENID token as well since PRD needs to
    // support both. Clients like EXUI is still using Bearer token and yet to migrate on OPENID. Currently all functional test cases are running
    // via OPENID tokens.
    @Test
    public void can_update_an_organisation_with_old_bearer_token() {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();
        professionalApiClient.updateOrganisationWithOldBearerToken(orgIdentifierResponse);
    }
}
