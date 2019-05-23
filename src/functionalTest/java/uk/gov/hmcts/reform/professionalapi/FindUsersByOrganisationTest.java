package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;


@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
public class FindUsersByOrganisationTest extends FunctionalTestSuite {


    @Test
    public void find_users_by_active_organisation_with_showDeleted_False() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String organisationIdentifier = (String) response.get("organisationIdentifier");
        assertThat(organisationIdentifier).isNotEmpty();
        professionalApiClient.updateOrganisation(organisationIdentifier);

        Map<String, Object> searchResponse = professionalApiClient.searchUsersByOrganisation(organisationIdentifier, "False", HttpStatus.OK);

        assertThat(searchResponse.get("users")).asList().isNotEmpty();

        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");
        HashMap professionalUsersResponse = professionalUsersResponses.get(0);

        assertThat(professionalUsersResponse.get("firstName")).isNotNull();
        assertThat(professionalUsersResponse.get("lastName")).isNotNull();
        assertThat(professionalUsersResponse.get("email")).isNotNull();
        assertThat(professionalUsersResponse.get("status")).isNotNull();
        assertThat(((List)professionalUsersResponse.get("roles")).size()).isEqualTo(0);
    }

    @Test
    public void find_users_by_active_organisation_with_showDeleted_True() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String organisationIdentifier = (String) response.get("organisationIdentifier");
        assertThat(organisationIdentifier).isNotEmpty();
        professionalApiClient.updateOrganisation(organisationIdentifier);

        Map<String, Object> searchResponse = professionalApiClient.searchUsersByOrganisation(organisationIdentifier, "False", HttpStatus.OK);

        assertThat(searchResponse.get("users")).asList().isNotEmpty();

        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");
        HashMap professionalUsersResponse = professionalUsersResponses.get(0);

        assertThat(professionalUsersResponse.get("firstName")).isNotNull();
        assertThat(professionalUsersResponse.get("lastName")).isNotNull();
        assertThat(professionalUsersResponse.get("email")).isNotNull();
        assertThat(professionalUsersResponse.get("status")).isNotNull();
        assertThat(((List)professionalUsersResponse.get("roles")).size()).isEqualTo(0);
    }

    @Test
    public void find_users_by_active_organisation_with_showDeleted_invalid() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String organisationIdentifier = (String) response.get("organisationIdentifier");
        assertThat(organisationIdentifier).isNotEmpty();
        professionalApiClient.updateOrganisation(organisationIdentifier);

        Map<String, Object> searchResponse = professionalApiClient.searchUsersByOrganisation(organisationIdentifier, "invalid", HttpStatus.OK);

        assertThat(searchResponse.get("users")).asList().isNotEmpty();

        List<HashMap> professionalUsersResponses = (List<HashMap>) searchResponse.get("users");
        HashMap professionalUsersResponse = professionalUsersResponses.get(0);

        assertThat(professionalUsersResponse.get("firstName")).isNotNull();
        assertThat(professionalUsersResponse.get("lastName")).isNotNull();
        assertThat(professionalUsersResponse.get("email")).isNotNull();
        assertThat(professionalUsersResponse.get("status")).isNotNull();
        assertThat(((List)professionalUsersResponse.get("roles")).size()).isEqualTo(0);
    }

    @Test
    public void find_users_for_non_active_organisation() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String organisationIdentifier = (String) response.get("organisationIdentifier");
        assertThat(organisationIdentifier).isNotEmpty();

        Map<String, Object> searchResponse = professionalApiClient.searchUsersByOrganisation(organisationIdentifier, "False", HttpStatus.OK);

        assertThat(searchResponse.get("users")).asList().isEmpty();
    }

    @Test
    public void find_users_for_non_existing_organisation() {

        professionalApiClient.searchUsersByOrganisation(UUID.randomUUID().toString(), "False", HttpStatus.NOT_FOUND);

    }
}
