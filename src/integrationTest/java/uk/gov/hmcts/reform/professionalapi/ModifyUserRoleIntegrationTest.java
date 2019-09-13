package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;

import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserProfileData;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures;

@Slf4j
public class ModifyUserRoleIntegrationTest extends AuthorizationEnabledIntegrationTest {

    private UUID settingUpOrganisation(String role) {
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");

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

        return UUID.fromString((String) newUserResponse.get("userIdentifier"));
    }


    @Test
    public void modify_roles_of_active_users_for_an_active_organisation_with_prd_admin_role_should_return_200() {

        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, "ACTIVE");
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-case-manager");

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        updateUserProfileRolesMock(HttpStatus.OK);

        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();

        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

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

        modifyUserProfileData.setRolesAdd(roles);

        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisation(modifyUserProfileData,organisationIdentifier, userIdentifier, hmctsAdmin);

        assertThat(response.get("statusCode")).isNotNull();
        assertThat(response.get("statusMessage")).isEqualTo("200");
    }

    @Ignore
    @Test
    public void modify_roles_of_active_users_for_an_active_organisation_with_pui_user_manager_role_should_return_200() {

        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();

        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        UUID userIdentifier = settingUpOrganisation("pui-user-manager");

        modifyUserProfileData.setRolesAdd(roles);

        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisationExternal(modifyUserProfileData, userIdentifier.toString(), puiUserManager);

    }
}
