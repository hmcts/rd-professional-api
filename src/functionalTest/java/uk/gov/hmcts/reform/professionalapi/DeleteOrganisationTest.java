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
        professionalApiClient.deleteOrganisation(orgIdentifier, hmctsAdmin, HttpStatus.NO_CONTENT);
        professionalApiClient.retrieveOrganisationDetails(orgIdentifier,hmctsAdmin, HttpStatus.NOT_FOUND);

    }

    @Test
    public void ac2_could_throw_not_found_error_when_delete_an_organisation_with_external_endpoint_404() {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifier).isNotEmpty();
        professionalApiClient.deleteOrganisationByExternalUser(orgIdentifier, HttpStatus.NOT_FOUND);

    }

    @Test
    public void ac3_error_when_delete_an_organisation_with_unknown_org_identifier_should_return_404() {

        String orgIdentifier = "C345EDF";
        professionalApiClient.deleteOrganisation(orgIdentifier, hmctsAdmin, HttpStatus.NOT_FOUND);
    }

    @Test
    public void ac4_error_when_delete_an_organisation_with_invalid_org_id_should_return_400() {

        String orgIdentifier = "C345DF";
        professionalApiClient.deleteOrganisation(orgIdentifier, hmctsAdmin, HttpStatus.BAD_REQUEST);
    }
}