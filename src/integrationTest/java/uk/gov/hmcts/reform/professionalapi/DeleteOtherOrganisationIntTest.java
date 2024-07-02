package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORGANISATION_IDENTIFIER_FORMAT_REGEX;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.otherOrganisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.otherOrganisationRequestWithAllFieldsAreUpdated;

class DeleteOtherOrganisationIntTest extends AuthorizationEnabledIntegrationTest {

    private String orgIdentifier;

    @Test
    void returns_204_when_delete_minimal_pending_other_organisation_successfully() {

        Map<String, Object> deleteResponse = deleteOtherOrganization();

        assertThat(deleteResponse.get("http_status")).isEqualTo(204);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .retrieveSingleOrganisationForV2Api(orgIdentifier,
                hmctsAdmin);

        assertThat(orgResponse.get("http_status").toString()).contains("404");
    }

    @Test
    void returns_204_when_delete_minimal_review_otherOrganisation_successfully() {

        Map<String, Object> deleteResponse = null;

        OrganisationOtherOrgsCreationRequest organisationCreationRequest = otherOrganisationRequestWithAllFields();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisationV2(organisationCreationRequest);

        orgIdentifier = (String) response.get(ORG_IDENTIFIER);

        updateOrganisation(orgIdentifier, hmctsAdmin, OrganisationStatus.REVIEW.name());


        deleteResponse = professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, orgIdentifier);

        assertThat(deleteResponse.get("http_status")).isEqualTo(204);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .retrieveSingleOrganisationForV2Api(orgIdentifier, hmctsAdmin);

        assertThat(orgResponse.get("http_status").toString()).contains("404");
    }

    @Test
    void return_forbidden_when_no_role_associated_with_end_point_to_delete_pending_otherOrganisation() {

        OrganisationOtherOrgsCreationRequest organisationCreationRequest = otherOrganisationRequestWithAllFields();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisationV2(organisationCreationRequest);

        orgIdentifier = (String) response.get(ORG_IDENTIFIER);
        assertThat(orgIdentifier).isNotNull();
        Map<String, Object> deleteResponse =
                professionalReferenceDataClient.deleteOrganisation(puiCaseManager, orgIdentifier);
        assertThat(deleteResponse.get("http_status")).isEqualTo("403");
    }


    @Test
    void returns_400_with_error_msg_when_delete_active_otherOrganisation_with_active_user_profile() {
        userProfileCreateUserWireMock(HttpStatus.resolve(201));
        String orgIdentifier = createAndActivateOtherOrganisation();

        Map<String, Object> deleteResponse =
                professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, orgIdentifier);
        assertThat(deleteResponse.get("http_status")).isEqualTo("400");
        assertThat((String) deleteResponse.get("response_body"))
                .contains("The organisation admin is not in Pending state");

    }

    @Test
    void returns_204_when_delete_active_otherOrganisation_with_one_pending_user_profile() {

        userProfileCreateUserWireMock(HttpStatus.resolve(201));
        String orgIdentifier = createAndActivateOtherOrganisation();
        getUserProfileByEmailWireMock(HttpStatus.resolve(200));
        deleteUserProfileMock(HttpStatus.resolve(204));
        Map<String, Object> deleteResponse =
                professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, orgIdentifier);
        assertThat(deleteResponse.get("http_status")).isEqualTo(204);
    }

    @Test
    void returns_400_when_delete_active_otherOrganisation_with_more_than_one__user_profile() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        userProfileCreateUserWireMock(HttpStatus.resolve(201));
        String orgIdentifier = createAndActivateOtherOrganisation();

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
    void returns_404_when_delete_active_otherOrganisation_with_external_endpoint() {

        userProfileCreateUserWireMock(HttpStatus.resolve(201));
        String orgIdentifier = createAndActivateOtherOrganisation();
        getUserProfileByEmailWireMock(HttpStatus.resolve(200));
        deleteUserProfileMock(HttpStatus.resolve(204));
        Map<String, Object> deleteResponse =
                professionalReferenceDataClient.deleteOrganisationExternal(hmctsAdmin, orgIdentifier);
        assertThat(deleteResponse.get("http_status")).isEqualTo("404");
    }

    private Map<String, Object> deleteOtherOrganization() {

        OrganisationOtherOrgsCreationRequest organisationCreationRequest = otherOrganisationRequestWithAllFields();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisationV2(organisationCreationRequest);

        orgIdentifier = (String) response.get(ORG_IDENTIFIER);

        assertThat(orgIdentifier).isNotNull();
        assertThat(orgIdentifier.length()).isEqualTo(LENGTH_OF_ORGANISATION_IDENTIFIER);
        assertThat(orgIdentifier.matches(ORGANISATION_IDENTIFIER_FORMAT_REGEX)).isTrue();
        return professionalReferenceDataClient.deleteOrganisation(hmctsAdmin, orgIdentifier);
    }

    public String createAndActivateOtherOrganisation() {
        String orgIdentifier = createOtherOrganisationRequest();
        updateOtherOrganisation(orgIdentifier, hmctsAdmin);
        return orgIdentifier;
    }

    public String createOtherOrganisationRequest() {
        OrganisationOtherOrgsCreationRequest organisationCreationRequest = otherOrganisationRequestWithAllFields();
        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
                .createOrganisationV2(organisationCreationRequest);
        return (String) responseForOrganisationCreation.get(ORG_IDENTIFIER);
    }

    public void updateOtherOrganisation(String organisationIdentifier, String role) {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        OrganisationOtherOrgsCreationRequest organisationUpdateRequest =
                otherOrganisationRequestWithAllFieldsAreUpdated();
        professionalReferenceDataClient
                .updateOrganisationForV2Api(organisationUpdateRequest, role, organisationIdentifier);
    }

}

