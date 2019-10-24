package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;

import java.util.*;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserProfileData;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures;


@Slf4j
public class ModifyUserStatusIntegrationTest extends AuthorizationEnabledIntegrationTest {

    private String settingUpOrganisation(String role) {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);

        List<String> userRoles = new ArrayList<>();
        userRoles.add(role);
        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email(randomAlphabetic(5) + "@email.com")
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, userCreationRequest, hmctsAdmin);

        return ((String) newUserResponse.get("userIdentifier"));
    }

    @Test
    public void ac1_modify_status_of_active_user_for_an_active_organisation_with_prd_admin_role_should_return_200() {

        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        List<String> userRoles = new ArrayList<>();
        userRoles.add(puiCaseManager);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        updateUserProfileRolesMock(HttpStatus.OK);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email(randomAlphabetic(5) + "@email.com")
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, userCreationRequest, hmctsAdmin);

        String userIdentifier = (String) newUserResponse.get("userIdentifier");
        ModifyUserProfileData modifyUserProfileData = createModifyUserProfileData();
        modifyUserProfileData.setIdamStatus("SUSPENDED");

        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisation(modifyUserProfileData, organisationIdentifier, userIdentifier, hmctsAdmin);
        assertThat(response.get("http_status")).isNotNull();
        assertThat(response.get("http_status")).isEqualTo("200 OK");
    }

    private ModifyUserProfileData  createModifyUserProfileData() {

        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();
        RoleName roleName1 = new RoleName(puiCaseManager);
        RoleName roleName2 = new RoleName(puiOrgManager);
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        modifyUserProfileData.setRolesAdd(roles);
        return modifyUserProfileData;
    }

    private void verifyDeleteRolesResponse(Map<String, Object> response) {

        assertThat(response.get("addRolesResponse")).isNotNull();
        Map<String, Object>  addRolesResponse =  (Map<String, Object>)response.get("addRolesResponse");

        assertThat(addRolesResponse.get("idamStatusCode")).isEqualTo("500");
        assertThat(addRolesResponse.get("idamMessage")).isEqualTo("Internal Server Error");
    }
}