package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;

import java.util.Map;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;


@RunWith(SpringIntegrationSerenityRunner.class)
public class RoleBasedAccessForUpdateTest extends AuthorizationEnabledIntegrationTest {

    @Test
    public void can_not_update_entities_other_than_hmcts_admin_organisation_should_returns_status_403() {

        String organisationIdentifier = createOrganisationRequest();
        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated().status("ACTIVE").build();

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, puiCaseManager, organisationIdentifier);

        assertThat(responseForOrganisationUpdate.get("http_status")).isEqualTo("403");
    }

    @Test
    public void can_not_update_entities_pui_user_manager_organisation_should_returns_status_403() {

        String organisationIdentifier = createOrganisationRequest();
        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated().status("ACTIVE").build();

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, puiUserManager, organisationIdentifier);

        assertThat(responseForOrganisationUpdate.get("http_status")).isEqualTo("403");
    }

    @Test
    public void can_not_update_entities_pui_organisation_manager_should_returns_status_403() {

        String organisationIdentifier = createOrganisationRequest();
        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated().status("ACTIVE").build();

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, puiOrgManager, organisationIdentifier);

        assertThat(responseForOrganisationUpdate.get("http_status")).isEqualTo("403");
    }

    @Test
    public void can_not_update_entities_pui_finance_manager_should_returns_status_403() {

        String organisationIdentifier = createOrganisationRequest();
        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated().status("ACTIVE").build();

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, puiFinanceManager, organisationIdentifier);

        assertThat(responseForOrganisationUpdate.get("http_status")).isEqualTo("403");
    }

    @Test
    public void can_not_update_entities_pui_case_manager_should_returns_status_403() {

        String organisationIdentifier = createOrganisationRequest();
        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated().status("ACTIVE").build();

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, puiCaseManager, organisationIdentifier);

        assertThat(responseForOrganisationUpdate.get("http_status")).isEqualTo("403");
    }

    public String createOrganisationRequest() {
        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().build();
        Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        return (String) responseForOrganisationCreation.get("organisationIdentifier");
    }

}
