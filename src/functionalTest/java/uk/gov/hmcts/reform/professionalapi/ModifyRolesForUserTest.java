package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;

import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class ModifyRolesForUserTest extends AuthorizationFunctionalTest {


    RequestSpecification bearerTokenForPuiUserManager;
    String orgIdentifierResponse;

    public RequestSpecification generateBearerTokenForPuiManager() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        String userEmail = generateRandomEmail();
        String lastName = "someLastName";
        String firstName = "someName";

        bearerTokenForPuiUserManager = professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager,
                firstName, lastName, userEmail);

        List<String> userRoles1 = new ArrayList<>();
        userRoles1.add("pui-organisation-manager");
        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles1)
                .build();
        professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest,
                HttpStatus.CREATED);

        return bearerTokenForPuiUserManager;
    }

    @Ignore("convert to integration test once RDCC-2050 is completed")
    @Test
    public void should_get_400_when_modify_roles_for_pending_user_internal() {

        Map<String, Object> createUserResponse = professionalApiClient.addNewUserToAnOrganisation(activeOrgId,
                hmctsAdmin, professionalApiClient.createNewUserRequest(), HttpStatus.CREATED);
        Map<String, Object> modifiedUserResponse = professionalApiClient
                .modifyUserToExistingUserForPrdAdmin(HttpStatus.BAD_REQUEST, getUserProfileAddRoleRequest(),
                        activeOrgId, (String) createUserResponse.get("userIdentifier"));
        assertThat(modifiedUserResponse.get("errorDescription")).isEqualTo("UserId status is not active");
        assertThat(modifiedUserResponse.get("errorMessage"))
                .isEqualTo("3 : There is a problem with your request. Please check and try again");
    }

    @Ignore("convert to integration test once RDCC-2050 is completed")
    @Test
    public void should_get_404_when_modify_roles_for_unknown_user_internal() {
        String unknownUserId = UUID.randomUUID().toString();
        Map<String, Object> modifiedUserResponse = professionalApiClient
                .modifyUserToExistingUserForPrdAdmin(HttpStatus.NOT_FOUND, getUserProfileAddRoleRequest(), activeOrgId,
                        unknownUserId);
        assertThat(modifiedUserResponse.get("errorDescription"))
                .isEqualTo("could not find user profile for userId: or status is not active " + unknownUserId);
        assertThat(modifiedUserResponse.get("errorMessage")).isEqualTo("4 : Resource not found");
    }

    public UserProfileUpdatedData getUserProfileAddRoleRequest() {
        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        userProfileUpdatedData.setRolesAdd(createAddRoleName());
        return userProfileUpdatedData;
    }

    private Set<RoleName> createAddRoleName() {

        RoleName roleName = new RoleName("pui-user-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName);
        return roles;
    }
}
