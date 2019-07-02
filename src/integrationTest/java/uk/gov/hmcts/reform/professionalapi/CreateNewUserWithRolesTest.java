package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;

public class CreateNewUserWithRolesTest extends Service2ServiceEnabledIntegrationTest {

    private static List<String> userRoles = new ArrayList<>();

    @BeforeClass
    public static void setUp() {
        userRoles.add("pui-user-manager");

    }

    private String createNewOrganisationWithAdmin(OrganisationCreationRequest organisationCreationRequest){
        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        return (String) organisationResponse.get("organisationIdentifier");
    }

    private Map<String, Object> addNewUserToOrganisation(String orgIdentifierResponse, NewUserCreationRequest userCreationRequest){
        return professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest);
    }

    private Map<String, Object>  addNewUserToOrganisation(OrganisationCreationRequest organisationCreationRequest, NewUserCreationRequest userCreationRequest){
        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) organisationResponse.get("organisationIdentifier");

        return professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest);
    }


    @Test
    public void post_request_adds_new_user_to_an_organisation() {

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        String organisationIdentifier = createNewOrganisationWithAdmin(organisationCreationRequest);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email("SOME@EMAIL.COM")
                .status("PENDING")
                .roles(userRoles)
                .build();

        Map<String, Object> newUserResponse = addNewUserToOrganisation(organisationCreationRequest, userCreationRequest);

        assertThat(newUserResponse).isNotNull();
    }


    @Test
    public void post_new_user_to_existing_organisation_with_email() {
        final String orgAdminEmail = "ORG.ADMIN@EMAIL.COM";
        final String expectAdminEmail = "org.admin@email.com";
        final String newUserEmail = "SOME.OTHER@EMAIL.COM";
        final String searchSecondUserEmail = "sOMe.OTHer@EmAiL.Com";
        final String expectSecondUserEmail = "some.other@email.com";

        OrganisationCreationRequest.OrganisationCreationRequestBuilder orgRequestBuilder = someMinimalOrganisationRequest(orgAdminEmail);
        OrganisationStatus status = OrganisationStatus.ACTIVE;
        OrganisationCreationRequest organisationCreationRequest = orgRequestBuilder.status(status).build();

        String organisationIdentifier = createNewOrganisationWithAdmin(organisationCreationRequest);

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationCreationRequest, organisationIdentifier);

        NewUserCreationRequest anotherUserRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email(newUserEmail)
                .status("ACTIVE")
                .roles(userRoles)
                .build();

        Map<String, Object> responseForNewUser = professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, anotherUserRequest);

        assertThat(responseForNewUser.get("http_status")).isEqualTo("201 CREATED");

        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(organisationIdentifier);


        assertThat(persistedOrganisation.getStatus()).isEqualTo(OrganisationStatus.ACTIVE);

        List<ProfessionalUser> users = persistedOrganisation.getUsers();

        assertThat(users).isNotNull();

        assertThat(users.get(0).getEmailAddress()).isEqualTo(expectAdminEmail);

        Map<String, Object> response =
                professionalReferenceDataClient.findUserByEmail(searchSecondUserEmail);

        String actualEmail = (String) response.get("email");

        assertThat(actualEmail).isEqualTo(expectSecondUserEmail);
    }

    @Test
    public void returns_404_when_organisation_identifier_not_found() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        //OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email("some@email.com")
                .status("PENDING")
                .roles(userRoles)
                .build();


        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation("AB83N5K", userCreationRequest);

        assertThat(newUserResponse.get("http_status")).isEqualTo("404");
    }


}
