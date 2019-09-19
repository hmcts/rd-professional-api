package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.createJurisdictions;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures;


public class CreateNewUserWithRolesTest extends AuthorizationEnabledIntegrationTest {

    @Autowired
    OrganisationRepository organisationRepository;
    @Autowired
    ProfessionalUserRepository professionalUserRepository;
    @Autowired
    PrdEnumRepository prdEnumRepository;

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
                .jurisdictions(OrganisationFixtures.createJurisdictions())
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


    @Test(expected = InvalidRequest.class)
    public void add_new_user_with_invalid_roles_returns_400_bad_request() {
        List<String> userRolesDuplicate = new ArrayList<>();
        userRolesDuplicate.add("pui-uer-manager");
        userRolesDuplicate.add("pui-user-manager");
        userRolesDuplicate.add("pui-case-manager");

        List<PrdEnum> prdEnums = prdEnumRepository.findAll();

        userCreationRequestValidator.validateRoles(userRolesDuplicate, prdEnums);
    }

    @Test(expected = InvalidRequest.class)
    public void add_new_user_with_invalid_roles_with_empty_space_returns_400_bad_request() {
        List<String> userRolesDuplicate = new ArrayList<>();
        userRolesDuplicate.add(" ");
        userRolesDuplicate.add("pui-user-manager");
        userRolesDuplicate.add("pui-case-manager");

        List<PrdEnum> prdEnums = prdEnumRepository.findAll();

        userCreationRequestValidator.validateRoles(userRolesDuplicate, prdEnums);
    }

    @Test
    public void add_new_user_with_same_role_multiple_times_in_request_only_returns_role_once() {
        List<String> userRolesDuplicate = new ArrayList<>();
        userRolesDuplicate.add("PUI-CASE-MANAGER ");
        userRolesDuplicate.add("pui-user-manager");
        userRolesDuplicate.add("pui-user-manager");

        List<PrdEnum> prdEnums = prdEnumRepository.findAll();

        List<String> validatedRoles = userCreationRequestValidator.validateRoles(userRolesDuplicate,prdEnums);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email("somenewuser@email.com")
                .roles(validatedRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
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
        assertThat(persistedProfessionalUser.getUserAttributes().size()).isEqualTo(2);
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
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("400");

        userCreationRequest = aNewUserCreationRequest()
                .firstName("fname")
                .lastName("")
                .email("somenewuser@email.com")
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();
        Map<String, Object> newUserResponse1 =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse1.get("http_status")).isEqualTo("400");

        userCreationRequest = aNewUserCreationRequest()
                .firstName("fname")
                .lastName("lname")
                .email("")
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
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
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("400");

        userCreationRequest = aNewUserCreationRequest()
                .firstName("fname")
                .lastName(null)
                .email("somenewuser@email.com")
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();
        Map<String, Object> newUserResponse1 =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse1.get("http_status")).isEqualTo("400");

        userCreationRequest = aNewUserCreationRequest()
                .firstName("fname")
                .lastName("lname")
                .email(null)
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
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
                .jurisdictions(OrganisationFixtures.createJurisdictions())
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
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("400");

    }
}