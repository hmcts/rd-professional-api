package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createJurisdictions;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

@RunWith(SpringIntegrationSerenityRunner.class)
public class CreateNewUserWithRolesTest extends AuthorizationEnabledIntegrationTest {

    @Autowired
    OrganisationRepository organisationRepository;
    @Autowired
    ProfessionalUserRepository professionalUserRepository;

    private OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();
    private NewUserCreationRequest userCreationRequest;
    private UserCreationRequestValidator userCreationRequestValidator;
    List<String> userRoles;

    @Before
    public void setUp() {
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
    public void post_request_adds_new_user_to_an_organisation() {
        List<String> userRoles = new ArrayList<>();

        userRoles.add("pui-user-manager");

        userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email("somenewuser@email.com")
                .roles(userRoles)
                .build();
    }

    @Test
    public void post_request_adds_new_user_to_an_active_organisation() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email("somenewuser@email.com")
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");

        OrganisationCreationRequest organisationUpdationRequest = someMinimalOrganisationRequest().status("ACTIVE").build();
        professionalReferenceDataClient.updateOrganisation(organisationUpdationRequest, hmctsAdmin, orgIdentifierResponse);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);

        String userIdentifierResponse = (String) newUserResponse.get("userIdentifier");

        assertThat(newUserResponse).isNotNull();
        assertEquals(newUserResponse.get("userIdentifier"), userIdentifierResponse);

        Organisation persistedOrganisation = organisationRepository.findByOrganisationIdentifier(orgIdentifierResponse);
        List<ProfessionalUser> users = professionalUserRepository.findByOrganisation(persistedOrganisation);
        assertThat(users.size()).isEqualTo(2);

        ProfessionalUser persistedProfessionalUser = professionalUserRepository.findByUserIdentifier(userIdentifierResponse);
        assertThat(persistedProfessionalUser).isNotNull();
    }

    @Test
    public void should_return_400_when_UP_fails_while_adding_new_user_to_an_active_organisation() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email("somenewuser@email.com")
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");

        OrganisationCreationRequest organisationUpdationRequest = someMinimalOrganisationRequest().status("ACTIVE").build();
        professionalReferenceDataClient.updateOrganisation(organisationUpdationRequest, hmctsAdmin, orgIdentifierResponse);

        userProfileCreateUserWireMock(HttpStatus.BAD_REQUEST);
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("400");

        assertThat(newUserResponse).isNotNull();

        Organisation persistedOrganisation = organisationRepository.findByOrganisationIdentifier(orgIdentifierResponse);
        assertThat(persistedOrganisation.getUsers().size()).isEqualTo(1);
    }

    @Test
    public void should_return_404_when_adding_new_user_to_an_pending_organisation() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("400");

        Organisation persistedOrganisation = organisationRepository.findByOrganisationIdentifier(orgIdentifierResponse);
        assertThat(persistedOrganisation.getUsers().size()).isEqualTo(1);

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
                .jurisdictions(createJurisdictions())
                .build();


        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation("AB83N5K", userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("404");
    }

    @Test
    public void returns_400_when_organisation_identifier_invalid() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email("some@email.com")
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();


        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation("invalid-org-id", userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("400");
    }


    @Test(expected = InvalidRequest.class)
    public void add_new_user_without_roles_returns_400_bad_request() {
        List<String> noUserRoles = new ArrayList<>();

        userCreationRequestValidator.validateRoles(noUserRoles);
    }

    @Test
    public void should_return_400_when_mandatory_fields_are_blank_while_adding_new_user() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");

        OrganisationCreationRequest organisationUpdationRequest = someMinimalOrganisationRequest().status("ACTIVE").build();
        professionalReferenceDataClient.updateOrganisation(organisationUpdationRequest, hmctsAdmin, orgIdentifierResponse);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("")
                .lastName("someLastName")
                .email("somenewuser@email.com")
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("400");

        userCreationRequest = aNewUserCreationRequest()
                .firstName("fname")
                .lastName("")
                .email("somenewuser@email.com")
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();
        Map<String, Object> newUserResponse1 =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse1.get("http_status")).isEqualTo("400");

        userCreationRequest = aNewUserCreationRequest()
                .firstName("fname")
                .lastName("lname")
                .email("")
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();
        Map<String, Object> newUserResponse2 =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse2.get("http_status")).isEqualTo("400");
    }

    @Test
    public void should_return_400_when_mandatory_fields_are_null_while_adding_new_user() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");

        OrganisationCreationRequest organisationUpdationRequest = someMinimalOrganisationRequest().status("ACTIVE").build();
        professionalReferenceDataClient.updateOrganisation(organisationUpdationRequest, hmctsAdmin, orgIdentifierResponse);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(null)
                .lastName("someLastName")
                .email("somenewuser@email.com")
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("400");

        userCreationRequest = aNewUserCreationRequest()
                .firstName("fname")
                .lastName(null)
                .email("somenewuser@email.com")
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();
        Map<String, Object> newUserResponse1 =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse1.get("http_status")).isEqualTo("400");

        userCreationRequest = aNewUserCreationRequest()
                .firstName("fname")
                .lastName("lname")
                .email(null)
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();
        Map<String, Object> newUserResponse2 =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse2.get("http_status")).isEqualTo("400");
    }

    @Test
    public void validate_email_for_invite_user_successfully() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");

        OrganisationCreationRequest organisationUpdationRequest = someMinimalOrganisationRequest().status("ACTIVE").build();
        professionalReferenceDataClient.updateOrganisation(organisationUpdationRequest, hmctsAdmin, orgIdentifierResponse);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("fname")
                .lastName("someLastName")
                .email("a.adison@email.com")
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("201 CREATED");

    }

    @Test
    public void validate_invalid_email_for_invite_user_and_throw_exception() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");

        OrganisationCreationRequest organisationUpdationRequest = someMinimalOrganisationRequest().status("ACTIVE").build();
        professionalReferenceDataClient.updateOrganisation(organisationUpdationRequest, hmctsAdmin, orgIdentifierResponse);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("fname")
                .lastName("someLastName")
                .email("a.adisonemail.com")
                .roles(userRoles)
                .jurisdictions(createJurisdictions())
                .build();
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("400");

    }

    @Test
    public void should_return_409_when_same_invited_new_user_is_super_user_of_pending_org() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        //create pending organisation #1
        OrganisationCreationRequest organisationCreationRequestForPendingOrg = someMinimalOrganisationRequest().build();

        Map<String, Object> pendingOrg =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequestForPendingOrg);

        //create and update any other organisation #2
        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().status("ACTIVE")
                .superUser(UserCreationRequest.aUserCreationRequest().firstName("fname").lastName("lname").jurisdictions(createJurisdictions()).email("someone1@gmail.com").build()).build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalReferenceDataClient.updateOrganisation(organisationCreationRequest, hmctsAdmin, orgIdentifierResponse);

        //invite same user used in creating pending organisation #1
        userCreationRequest.setEmail(organisationCreationRequestForPendingOrg.getSuperUser().getEmail());
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("409");
        assertThat((String)newUserResponse.get("response_body")).contains("\"errorDescription\":\"409 User already exists\"");
    }
}