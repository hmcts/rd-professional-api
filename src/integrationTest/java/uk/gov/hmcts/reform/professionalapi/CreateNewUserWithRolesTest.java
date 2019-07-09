package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;


public class CreateNewUserWithRolesTest extends AuthorizationEnabledIntegrationTest {

    private static final String role = "pui-user-manager";

    private String createNewOrganisationWithAdmin(OrganisationCreationRequest organisationCreationRequest) {
        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        return (String) organisationResponse.get("organisationIdentifier");
    }

    private Map<String, Object>  addNewUserToOrganisation(OrganisationCreationRequest organisationCreationRequest, NewUserCreationRequest userCreationRequest) {
        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) organisationResponse.get("organisationIdentifier");

        return professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);
    }


    @Test
    public void post_request_adds_new_user_to_an_organisation() {

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        String organisationIdentifier = createNewOrganisationWithAdmin(organisationCreationRequest);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email("SOME@EMAIL.COM")
                .roles(userRoles)
                .build();


        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");

        Map<String, Object> newUserResponse = addNewUserToOrganisation(organisationCreationRequest, userCreationRequest);

        assertThat(newUserResponse).isNotNull();
    }



    @Test
    public void create_new_active_organisation_with_super_user_and_additional_user() {
        final String orgAdminEmail = "ORG.ADMIN@EMAIL.COM";
        final String expectAdminEmail = "org.admin@email.com";
        final String newUserEmail = "SOME.OTHER@EMAIL.COM";
        final String searchSecondUserEmail = "sOMe.OTHer@EmAiL.Com";
        final String expectSecondUserEmail = "some.other@email.com";

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        OrganisationCreationRequest.OrganisationCreationRequestBuilder orgRequestBuilder = someMinimalOrganisationRequest(orgAdminEmail);

        OrganisationCreationRequest organisationCreationRequest = orgRequestBuilder.status(OrganisationStatus.ACTIVE).build();

        String organisationIdentifier = createNewOrganisationWithAdmin(organisationCreationRequest);

        assertThat(organisationIdentifier).isNotNull();

        Map<String, Object> responseForOrganisationUpdate = professionalReferenceDataClient.updateOrganisation(organisationCreationRequest, "prd-admin", organisationIdentifier);

        assertThat(responseForOrganisationUpdate.get("http_status")).isEqualTo(200);

        NewUserCreationRequest anotherUserRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email(newUserEmail)
                .roles(userRoles)
                .build();

        Map<String, Object> responseForNewUser = professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, anotherUserRequest, "prd-admin");

        assertThat(responseForNewUser.get("http_status")).isEqualTo("201 CREATED");

        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(organisationIdentifier);

        assertThat(persistedOrganisation.getStatus()).isEqualTo(OrganisationStatus.ACTIVE);

        List<ProfessionalUser> users = persistedOrganisation.getUsers();

        assertThat(users).isNotNull();

        assertThat(users.get(0).getEmailAddress()).isEqualTo(expectAdminEmail);

        Map<String, Object> response =
                professionalReferenceDataClient.findUserByEmail(searchSecondUserEmail, "prd-admin");

        String actualEmail = (String) response.get("email");

        assertThat(actualEmail).isEqualTo(expectSecondUserEmail);

    }

    @Test
    public void returns_404_when_organisation_identifier_not_found() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email("some@email.com")
                .roles(userRoles)
                .build();


        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation("AB83N5K", userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("404");
    }

}
