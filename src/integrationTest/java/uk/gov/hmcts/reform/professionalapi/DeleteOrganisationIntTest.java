package uk.gov.hmcts.reform.professionalapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

@RunWith(SpringIntegrationSerenityRunner.class)
class DeleteOrganisationIntTest extends AuthorizationEnabledIntegrationTest {

    private String orgIdentifier;

    @Test
    void returns_204_when_delete_minimal_pending_organisation_successfully() {

        Map<String, Object> deleteResponse = deleteOrganization();

        assertThat(deleteResponse.get("http_status")).isEqualTo(204);

        Map<String, Object> orgResponse = professionalReferenceDataClient.retrieveSingleOrganisation(orgIdentifier,
            hmctsAdmin);

        assertThat(orgResponse.get("http_status").toString().contains("OK"));
    }

    @Test
    void return_forbidden_when_no_role_associated_with_end_point_to_delete_pending_organisation() {

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().build();

        Map<String, Object> response =
            professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgIdentifier = (String) response.get(ORG_IDENTIFIER);
        assertThat(orgIdentifier).isNotNull();
        Map<String, Object> deleteResponse =
            professionalReferenceDataClient.deleteOrganisation(puiCaseManager, orgIdentifier);
        assertThat(deleteResponse.get("http_status")).isEqualTo("403");
    }

    @Test
    void return_404_when_un_known_org_identifier_in_the_request_to_delete_pending_organisation() {

        Map<String, Object> deleteResponse =
            professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, "O12DEF3");
        assertThat(deleteResponse.get("http_status")).isEqualTo("404");
    }

    @Test
    void return_400_when_invalid_org_identifier_in_the_request_to_delete_pending_organisation() {

        Map<String, Object> deleteResponse =
            professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, "O12DEF");
        assertThat(deleteResponse.get("http_status")).isEqualTo("400");
    }

    @Test
    void returns_400_with_error_msg_when_delete_active_organisation_with_active_user_profile() {
        userProfileCreateUserWireMock(HttpStatus.resolve(201));
        String orgIdentifier = createAndActivateOrganisation();

        Map<String, Object> deleteResponse =
            professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, orgIdentifier);
        assertThat(deleteResponse.get("http_status")).isEqualTo("400");
        assertThat((String) deleteResponse.get("response_body"))
            .contains("The organisation admin is not in Pending state");

    }

    @Test
    void returns_204_when_delete_active_organisation_with_one_pending_user_profile() {

        userProfileCreateUserWireMock(HttpStatus.resolve(201));
        String orgIdentifier = createAndActivateOrganisation();
        getUserProfileByEmailWireMock(HttpStatus.resolve(200));
        deleteUserProfileMock(HttpStatus.resolve(204));
        Map<String, Object> deleteResponse =
            professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, orgIdentifier);
        assertThat(deleteResponse.get("http_status")).isEqualTo(204);
    }

    @Test
    void returns_400_when_delete_active_organisation_with_more_than_one__user_profile() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        userProfileCreateUserWireMock(HttpStatus.resolve(201));
        String orgIdentifier = createAndActivateOrganisation();

        Map<String, Object> newUserResponse = professionalReferenceDataClient
                .addUserToOrganisation(orgIdentifier,
                        inviteUserCreationRequest("somenewuser@email.com", userRoles), hmctsAdmin);

        String userIdentifierResponse = (String) newUserResponse.get(USER_IDENTIFIER);

        assertThat(newUserResponse).isNotNull();
        assertEquals(newUserResponse.get(USER_IDENTIFIER), userIdentifierResponse);

        Map<String, Object> deleteResponse =
                professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, orgIdentifier);
        assertThat(deleteResponse.get("http_status")).isEqualTo("400");
    }

    @Test
    void returns_404_when_delete_active_organisation_with_external_endpoint() {

        userProfileCreateUserWireMock(HttpStatus.resolve(201));
        String orgIdentifier = createAndActivateOrganisation();
        getUserProfileByEmailWireMock(HttpStatus.resolve(200));
        deleteUserProfileMock(HttpStatus.resolve(204));
        Map<String, Object> deleteResponse =
                professionalReferenceDataClient.deleteOrganisationExternal(hmctsAdmin, orgIdentifier);
        assertThat(deleteResponse.get("http_status")).isEqualTo("404");
    }

    private Map<String, Object> deleteOrganization() {

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        Map<String, Object> response =
            professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        orgIdentifier = (String) response.get(ORG_IDENTIFIER);

        assertThat(orgIdentifier).isNotNull();
        assertThat(orgIdentifier.length()).isEqualTo(LENGTH_OF_ORGANISATION_IDENTIFIER);
        assertThat(orgIdentifier.matches(ORGANISATION_IDENTIFIER_FORMAT_REGEX)).isTrue();
        return professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, orgIdentifier);
    }
}
