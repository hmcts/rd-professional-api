package uk.gov.hmcts.reform.professionalapi;

import java.util.Map;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;


@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Ignore
public class FindUsersByOrganisationTest extends FunctionalTestSuite {

    @Test
    public void find_users_by_active_organisation_with_showDeleted_False() {
        validateUsers(professionalApiClient.searchUsersByOrganisation(createAndUpdateOrganisationToActive(), "False", HttpStatus.OK));
    }

    @Test
    public void find_users_by_active_organisation_with_showDeleted_True() {
        validateUsers(professionalApiClient.searchUsersByOrganisation(createAndUpdateOrganisationToActive(), "True", HttpStatus.OK));
    }

    @Test
    public void find_users_by_active_organisation_with_showDeleted_invalid() {
        validateUsers(professionalApiClient.searchUsersByOrganisation(createAndUpdateOrganisationToActive(), "invalid", HttpStatus.OK));
    }

    @Test
    public void find_users_for_non_active_organisation() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String organisationIdentifier = (String) response.get("organisationIdentifier");
        Map<String, Object> searchResponse = professionalApiClient.searchUsersByOrganisation(organisationIdentifier, "False", HttpStatus.NOT_FOUND);
    }

    @Test
    public void find_users_for_non_existing_organisation() {
        professionalApiClient.searchUsersByOrganisation("Q1VHDF3", "False", HttpStatus.NOT_FOUND);
    }
}
