package uk.gov.hmcts.reform.professionalapi;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

class CreateNewUserWithRolesTest extends AuthorizationEnabledIntegrationTest {

    @Autowired
    OrganisationRepository organisationRepository;
    @Autowired
    ProfessionalUserRepository professionalUserRepository;

    private OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();
    private NewUserCreationRequest userCreationRequest;
    private UserCreationRequestValidator userCreationRequestValidator;
    List<String> userRoles;

    @BeforeEach
    void setUp() {
        userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email("somenewuser@email.com")
                .roles(userRoles)
                .build();
    }

    @Test
    void post_request_adds_new_user_to_an_active_organisation() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        Map<String, Object> response = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgIdentifierResponse = (String) response.get(ORG_IDENTIFIER);

        OrganisationCreationRequest organisationUpdationRequest = someMinimalOrganisationRequest().status("ACTIVE")
                .build();
        professionalReferenceDataClient.updateOrganisation(organisationUpdationRequest, hmctsAdmin,
                orgIdentifierResponse);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        String userIdentifier = retrieveSuperUserIdFromOrganisationId(orgIdentifierResponse);

        Map<String, Object> newUserResponse = professionalReferenceDataClient
                .addUserToOrganisationWithUserId(orgIdentifierResponse,
                        inviteUserCreationRequest("somenewuser@email.com", userRoles), hmctsAdmin,
                        userIdentifier);

        String userIdentifierResponse = (String) newUserResponse.get(USER_IDENTIFIER);

        assertThat(newUserResponse).isNotNull();
        assertEquals(newUserResponse.get(USER_IDENTIFIER), userIdentifierResponse);

        Organisation updatedOrganisation = organisationRepository.findByOrganisationIdentifier(orgIdentifierResponse);
        ProfessionalUser updatedUser = professionalUserRepository.findByOrganisationAndUserIdentifier(
                updatedOrganisation,USER_IDENTIFIER);
        assertThat(updatedUser).isNotNull();

        ProfessionalUser persistedProfessionalUser = professionalUserRepository
                .findByUserIdentifier(userIdentifierResponse);
        assertThat(persistedProfessionalUser).isNotNull();
    }

    @Test
    void post_request_adds_new_user_with_caa_roles_to_an_active_organisation() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        userRoles.add(puiCaa);
        userRoles.add(caseworkerCaa);

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(someMinimalOrganisationRequest().build());
        String orgIdentifierResponse = (String) response.get(ORG_IDENTIFIER);

        professionalReferenceDataClient.updateOrganisation(
                someMinimalOrganisationRequest().status("ACTIVE").build(), hmctsAdmin, orgIdentifierResponse);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        String userIdentifier = retrieveSuperUserIdFromOrganisationId(orgIdentifierResponse);

        Map<String, Object> newUserResponse = professionalReferenceDataClient
                .addUserToOrganisationWithUserId(orgIdentifierResponse,
                        inviteUserCreationRequest("somenewuser@email.com", userRoles), hmctsAdmin,
                        userIdentifier);

        String userIdentifierResponse = (String) newUserResponse.get(USER_IDENTIFIER);

        assertThat(newUserResponse).isNotNull();
        assertEquals(newUserResponse.get(USER_IDENTIFIER), userIdentifierResponse);

        Organisation updatedOrganisation = organisationRepository.findByOrganisationIdentifier(orgIdentifierResponse);
        ProfessionalUser updatedUser = professionalUserRepository.findByOrganisationAndUserIdentifier(
                updatedOrganisation,USER_IDENTIFIER);
        assertThat(updatedUser).isNotNull();

        ProfessionalUser persistedProfessionalUser = professionalUserRepository
                .findByUserIdentifier(userIdentifierResponse);
        assertThat(persistedProfessionalUser).isNotNull();
    }

    @Test
    void should_return_400_when_UP_fails_while_adding_new_user_to_an_active_organisation() {
        userProfileCreateUserWireMock(HttpStatus.CREATED);

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        Map<String, Object> response = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get(ORG_IDENTIFIER);

        OrganisationCreationRequest organisationUpdationRequest = someMinimalOrganisationRequest().status("ACTIVE")
                .build();
        professionalReferenceDataClient.updateOrganisation(organisationUpdationRequest, hmctsAdmin,
                orgIdentifierResponse);

        String userIdentifier = retrieveSuperUserIdFromOrganisationId(orgIdentifierResponse);

        userProfileCreateUserWireMock(HttpStatus.BAD_REQUEST);
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisationWithUserId(orgIdentifierResponse,
                        inviteUserCreationRequest("somenewuser@email.com", userRoles), hmctsAdmin,
                        userIdentifier);

        assertThat(newUserResponse.get("http_status")).isEqualTo("400");
        assertThat(newUserResponse).isNotNull();

        Organisation persistedOrganisation = organisationRepository.findByOrganisationIdentifier(orgIdentifierResponse);
        assertThat(persistedOrganisation.getUsers().size()).isEqualTo(1);
    }

    @Test
    void should_return_404_when_adding_new_user_to_an_pending_organisation() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get(ORG_IDENTIFIER);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest,
                        hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("400");

        Organisation persistedOrganisation = organisationRepository.findByOrganisationIdentifier(orgIdentifierResponse);
        assertThat(persistedOrganisation.getUsers().size()).isEqualTo(1);

    }

    @Test
    void returns_404_when_organisation_identifier_not_found() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation("AB83N5K",
                        inviteUserCreationRequest("some@email.com", userRoles), hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("404");
    }

    @Test
    void returns_400_when_organisation_identifier_invalid() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation("invalid-org-id",
                        inviteUserCreationRequest("some@email.com", userRoles), hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("400");
    }


    @Test
    void add_new_user_without_roles_returns_400_bad_request() {
        List<String> noUserRoles = new ArrayList<>();

        assertThrows(InvalidRequest.class, () ->
                userCreationRequestValidator.validateRoles(noUserRoles));
    }

    @Test
    void should_return_400_when_mandatory_fields_are_blank_while_adding_new_user() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgIdentifierResponse = (String) response.get(ORG_IDENTIFIER);

        OrganisationCreationRequest organisationUpdationRequest = someMinimalOrganisationRequest().status("ACTIVE")
                .build();
        professionalReferenceDataClient.updateOrganisation(organisationUpdationRequest, hmctsAdmin,
                orgIdentifierResponse);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse,
                        inviteUserCreationRequest("", userRoles), hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("400");

        userCreationRequest = aNewUserCreationRequest()
                .firstName("fname")
                .lastName("")
                .email("somenewuser@email.com")
                .roles(userRoles)
                .build();
        Map<String, Object> newUserResponse1 =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest,
                        hmctsAdmin);

        assertThat(newUserResponse1.get("http_status")).isEqualTo("400");

        userCreationRequest = aNewUserCreationRequest()
                .firstName("fname")
                .lastName("lname")
                .email("")
                .roles(userRoles)
                .build();
        Map<String, Object> newUserResponse2 =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest,
                        hmctsAdmin);

        assertThat(newUserResponse2.get("http_status")).isEqualTo("400");
    }

    @Test
    void should_return_400_when_mandatory_fields_are_null_while_adding_new_user() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgIdentifierResponse = (String) response.get(ORG_IDENTIFIER);

        OrganisationCreationRequest organisationUpdationRequest = someMinimalOrganisationRequest().status("ACTIVE")
                .build();
        professionalReferenceDataClient.updateOrganisation(organisationUpdationRequest, hmctsAdmin,
                orgIdentifierResponse);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(null)
                .lastName("someLastName")
                .email("somenewuser@email.com")
                .roles(userRoles)
                .build();
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest,
                        hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("400");

        userCreationRequest = aNewUserCreationRequest()
                .firstName("fname")
                .lastName(null)
                .email("somenewuser@email.com")
                .roles(userRoles)
                .build();
        Map<String, Object> newUserResponse1 =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest,
                        hmctsAdmin);

        assertThat(newUserResponse1.get("http_status")).isEqualTo("400");

        userCreationRequest = aNewUserCreationRequest()
                .firstName("fname")
                .lastName("lname")
                .email(null)
                .roles(userRoles)
                .build();
        Map<String, Object> newUserResponse2 =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest,
                        hmctsAdmin);

        assertThat(newUserResponse2.get("http_status")).isEqualTo("400");
    }

    @Test
    void validate_email_for_invite_user_successfully() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgIdentifierResponse = (String) response.get(ORG_IDENTIFIER);

        OrganisationCreationRequest organisationUpdationRequest = someMinimalOrganisationRequest()
                .status("ACTIVE").build();
        professionalReferenceDataClient.updateOrganisation(organisationUpdationRequest, hmctsAdmin,
                orgIdentifierResponse);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        String userIdentifier = retrieveSuperUserIdFromOrganisationId(orgIdentifierResponse);

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisationWithUserId(orgIdentifierResponse,
                        inviteUserCreationRequest("test@test.com", userRoles), hmctsAdmin,
                        userIdentifier);

        assertThat(newUserResponse.get("http_status")).isEqualTo("201 CREATED");

    }

    @Test
    void validate_invalid_email_for_invite_user_and_throw_exception() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgIdentifierResponse = (String) response.get(ORG_IDENTIFIER);

        OrganisationCreationRequest organisationUpdationRequest = someMinimalOrganisationRequest()
                .status("ACTIVE").build();
        professionalReferenceDataClient.updateOrganisation(organisationUpdationRequest, hmctsAdmin,
                orgIdentifierResponse);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse,
                        inviteUserCreationRequest("a.adisonemail.com", userRoles), hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("400");

    }

    @Test
    void should_return_409_when_same_invited_new_user_is_super_user_of_pending_org() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        //create pending organisation #1
        OrganisationCreationRequest organisationCreationRequestForPendingOrg = someMinimalOrganisationRequest().build();

        Map<String, Object> pendingOrg =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequestForPendingOrg);

        //create and update any other organisation #2
        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().status("ACTIVE")
                .superUser(UserCreationRequest.aUserCreationRequest().firstName("fname").lastName("lname")
                        .email("someone1@gmail.com").build()).build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgIdentifierResponse = (String) response.get(ORG_IDENTIFIER);
        professionalReferenceDataClient.updateOrganisation(organisationCreationRequest, hmctsAdmin,
                orgIdentifierResponse);

        //invite same user used in creating pending organisation #1
        userCreationRequest.setEmail(organisationCreationRequestForPendingOrg.getSuperUser().getEmail());
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest,
                        hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("409");
        assertThat((String) newUserResponse.get("response_body"))
                .contains("\"errorDescription\":\"409 User already exists\"");
    }

    @Test
    void super_user_can_have_caa_roles_fpla_or_iac_roles_not_puiCaa_caseworkerCaa() {
        userProfileCreateUserWireMockWithExtraRoles();
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

        List<String> userRoles = new ArrayList<>();
        userRoles.add(puiUserManager);
        userRoles.add("caseworker");

        String userIdentifier = retrieveSuperUserIdFromOrganisationId(organisationIdentifier);

        userProfileCreateUserWireMockWithExtraRoles();

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisationWithUserId(organisationIdentifier,
                        inviteUserCreationRequest(randomAlphabetic(5) + "@email.com", userRoles),
                        hmctsAdmin, userIdentifier);

        String id = (String) newUserResponse.get(USER_IDENTIFIER);

        userProfileCreateUserWireMockWithExtraRoles();

        Map<String, Object> response = professionalReferenceDataClient.findUsersByOrganisationWithReturnRoles(
                "true", puiCaseManager, id);

        assertThat(response.get(ORG_IDENTIFIER)).isNotNull();
        assertThat(((List<ProfessionalUsersResponse>) response.get("users")).size()).isEqualTo(1);
        List<HashMap> professionalUsersResponses = (List<HashMap>) response.get("users");

        assertThat(professionalUsersResponses.get(0).get(USER_IDENTIFIER)).isNotNull();
        assertThat(professionalUsersResponses.get(0).get("firstName")).isNotNull();
        assertThat(professionalUsersResponses.get(0).get("lastName")).isNotNull();
        assertThat(professionalUsersResponses.get(0).get("email")).isNotNull();
        assertThat(professionalUsersResponses.get(0).get("idamStatus")).isNotNull();
        assertThat(((List) professionalUsersResponses.get(0).get("roles")).size()).isEqualTo(2);
        assertTrue(((List) professionalUsersResponses.get(0).get("roles"))
                .containsAll(asList("caseworker", "pui-organisation-manager")));
    }
}