package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;

import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
public class ModifyUserStatusIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    public void ac1_modify_status_of_active_user_for_an_active_organisation_with_prd_admin_role_should_return_200() {

        //create and update organisation
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);

        //create new user
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        List<String> userRoles = new ArrayList<>();
        userRoles.add(puiCaseManager);
        updateUserProfileRolesMock(HttpStatus.OK);

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, inviteUserCreationRequest(randomAlphabetic(5) + "@email.com", userRoles), hmctsAdmin);
        String userIdentifier = (String) newUserResponse.get("userIdentifier");

        //modify user details
        UserProfileUpdatedData userProfileUpdatedData = createModifyUserProfileData();
        userProfileUpdatedData.setIdamStatus("SUSPENDED");
        updateUserProfileRolesMock(HttpStatus.OK);

        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisation(userProfileUpdatedData, organisationIdentifier, userIdentifier, hmctsAdmin);

        //validate overall response should be 200 always
        assertThat(response.get("http_status")).isNotNull();
        assertThat(response.get("http_status")).isEqualTo("200 OK");

        //internal response for update status should be 200
        Map<String, Object> addRolesResponse = ((Map<String, Object>) response.get("statusUpdateResponse"));
        assertThat(addRolesResponse.get("idamStatusCode")).isEqualTo("200");
        assertThat(addRolesResponse.get("idamMessage")).isEqualTo("Success");

    }


    @Test
    public void ac2_modify_status_of_active_user_should_return_400_when_up_fails_with_400() {

        //create and update organisation
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);

        //create new user
        List<String> userRoles = new ArrayList<>();
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        userRoles.add(puiCaseManager);

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, inviteUserCreationRequest(randomAlphabetic(5) + "@email.com", userRoles), hmctsAdmin);

        //modify user details
        String userIdentifier = (String) newUserResponse.get("userIdentifier");
        UserProfileUpdatedData userProfileUpdatedData = createModifyUserProfileData();
        userProfileUpdatedData.setIdamStatus("SUSPENDED");
        updateUserProfileRolesMock(HttpStatus.BAD_REQUEST);

        //validate overall response should be 200 always
        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisation(userProfileUpdatedData, organisationIdentifier, userIdentifier, hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("400");
        assertThat(response.get("response_body")).isEqualTo("{\"errorMessage\":\"400\",\"errorDescription\":\"BAD REQUEST\",\"timeStamp\":\"23:10\"}");
    }

    private UserProfileUpdatedData createModifyUserProfileData() {

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        RoleName roleName1 = new RoleName(puiCaseManager);
        RoleName roleName2 = new RoleName(puiOrgManager);
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        userProfileUpdatedData.setRolesAdd(roles);
        return userProfileUpdatedData;
    }
}