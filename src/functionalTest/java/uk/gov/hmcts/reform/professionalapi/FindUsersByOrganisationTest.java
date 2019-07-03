package uk.gov.hmcts.reform.professionalapi;

import java.util.Map;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
public class FindUsersByOrganisationTest extends AuthorizationFunctionalTest {


    @Test
    public void find_users_by_active_organisation_with_showDeleted_False() {

        validateUsers(professionalApiClient.searchUsersByOrganisation(createAndUpdateOrganisationToActive(hmctsAdmin), hmctsAdmin, "False", HttpStatus.OK));
    }

    @Test
    public void find_users_by_active_organisation_with_showDeleted_True() {
        validateUsers(professionalApiClient.searchUsersByOrganisation(createAndUpdateOrganisationToActive(hmctsAdmin), hmctsAdmin,"True", HttpStatus.OK));
    }

    @Test
    public void find_users_by_active_organisation_with_showDeleted_invalid() {
        validateUsers(professionalApiClient.searchUsersByOrganisation(createAndUpdateOrganisationToActive(hmctsAdmin), hmctsAdmin,"invalid", HttpStatus.OK));
    }

    @Test
    public void find_users_for_non_active_organisation() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String organisationIdentifier = (String) response.get("organisationIdentifier");
        Map<String, Object> searchResponse = professionalApiClient.searchUsersByOrganisation(organisationIdentifier, hmctsAdmin,"False", HttpStatus.NOT_FOUND);
    }

    @Test
    public void find_users_for_non_existing_organisation() {
        professionalApiClient.searchUsersByOrganisation("Q1VHDF3", hmctsAdmin,"False", HttpStatus.NOT_FOUND);
    }

}
