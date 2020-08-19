package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
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

    @Test
    public void can_throw_Unauthorized_Error_code_without_bearertoken_to_update_an_organisation_401() {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();
        professionalApiClient.updateOrganisationWithoutBearerToken(hmctsAdmin,orgIdentifierResponse,
                HttpStatus.UNAUTHORIZED);
    }
}
