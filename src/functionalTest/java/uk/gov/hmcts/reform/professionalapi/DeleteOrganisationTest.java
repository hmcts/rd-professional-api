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
public class DeleteOrganisationTest extends AuthorizationFunctionalTest {

    @Test
    public void ac1_can_delete_an_organisation_with_valid_org_identifier_by_prd_admin() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifier).isNotEmpty();
        Map<String, Object> deleteResponse = professionalApiClient.deleteOrganisation(orgIdentifier, hmctsAdmin, HttpStatus.NO_CONTENT);
        assertThat(deleteResponse).isNotEmpty();

    }

    @Test
    public void ac2_can_throw_forbidden_error_when_delete_an_organisation_with_in_valid_role_403() {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifier).isNotEmpty();
        Map<String, Object> deleteResponse = professionalApiClient.deleteOrganisation(orgIdentifier, puiCaseManager, HttpStatus.FORBIDDEN);
        assertThat(deleteResponse).isNotEmpty();
    }

    @Test
    public void ac3_error_when_delete_an_organisation_with_unknown_org_identifier_should_return_404() {

        String orgIdentifier = "C345EDF";
        Map<String, Object> deleteResponse = professionalApiClient.deleteOrganisation(orgIdentifier, hmctsAdmin, HttpStatus.NOT_FOUND);
        assertThat(deleteResponse).isNotEmpty();
    }
}