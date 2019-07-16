package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;


public class CreateNewUserWithRolesTest extends AuthorizationEnabledIntegrationTest {

    @Autowired
    OrganisationRepository organisationRepository;
    @Autowired
    ProfessionalUserRepository professionalUserRepository;

    private OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();
    private NewUserCreationRequest userCreationRequest;
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
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");

        OrganisationCreationRequest organisationUpdationRequest = someMinimalOrganisationRequest().status(OrganisationStatus.ACTIVE).build();
        professionalReferenceDataClient.updateOrganisation(organisationUpdationRequest, hmctsAdmin, orgIdentifierResponse);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest, hmctsAdmin);

        String userIdentifierResponse = (String) newUserResponse.get("userIdentifier");

        assertThat(newUserResponse).isNotNull();
        assertEquals(newUserResponse.get("userIdentifier"), userIdentifierResponse);

        Organisation persistedOrganisation = organisationRepository.findByOrganisationIdentifier(orgIdentifierResponse);
        assertThat(persistedOrganisation.getUsers().size()).isEqualTo(2);

        ProfessionalUser persistedProfessionalUser = professionalUserRepository.findByUserIdentifier(UUID.fromString(userIdentifierResponse));
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
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");

        OrganisationCreationRequest organisationUpdationRequest = someMinimalOrganisationRequest().status(OrganisationStatus.ACTIVE).build();
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

        assertThat(newUserResponse.get("http_status")).isEqualTo("404");

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
                .build();


        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation("AB83N5K", userCreationRequest, hmctsAdmin);

        assertThat(newUserResponse.get("http_status")).isEqualTo("404");
    }


}
