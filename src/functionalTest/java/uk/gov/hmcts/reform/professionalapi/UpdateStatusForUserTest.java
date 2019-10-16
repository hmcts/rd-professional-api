package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;

import io.restassured.specification.RequestSpecification;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserProfileData;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;
import uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures;



@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class UpdateStatusForUserTest extends ModifyRolesForUserTest {

    @Test
    public void ac1_modify_status_existing_user_from_Active_to_Pending() {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifier).isNotEmpty();

        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);
        IdamOpenIdClient idamOpenIdClient = new IdamOpenIdClient(configProperties);
        String email = idamOpenIdClient.createUser("pui-organisation-manager");
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest(email);
        assertThat(newUserCreationRequest).isNotNull();

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, newUserCreationRequest);
        assertThat(newUserResponse).isNotNull();

        Map<String, Object> searchResponse = professionalApiClient.searchOrganisationUsersByStatusInternal(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");
        Map professionalUsersResponse = professionalUsersResponses.get(1);

        String userId = (String) professionalUsersResponse.get("userIdentifier");
        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();
        modifyUserProfileData.setIdamStatus(IdamStatus.SUSPENDED.toString());
        professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, modifyUserProfileData, orgIdentifier, userId);

        //check status was changed from ACTIVE to SUSPENDED
        searchUserValidateStatus(userId, "SUSPENDED", orgIdentifier);

        modifyUserProfileData.setIdamStatus(IdamStatus.ACTIVE.toString());
        professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, modifyUserProfileData, orgIdentifier, userId);
        //check status was changed back to ACTIVE after being changed to SUSPENDED
        searchUserValidateStatus(userId, "ACTIVE", orgIdentifier);
    }

    @Test
    public void ac3_modify_status_pending_user_throws_400() {

        Map<String, Object> searchResponse = professionalApiClient.searchOrganisationUsersByStatusExternal(HttpStatus.OK, generateBearerTokenForPuiManagerWithPendingUser(), "PENDING");


    }

    public RequestSpecification generateBearerTokenForPuiManagerWithPendingUser() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        bearerTokenForPuiUserManager = professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, userEmail);

        List<String> userRoles1 = new ArrayList<>();
        userRoles1.add("pui-organisation-manager");
        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles1)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();
        professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest);

        NewUserCreationRequest userCreationRequest1 = aNewUserCreationRequest()
                .firstName("Leeroy")
                .lastName("Jenkins")
                .email("leeroy@jenkins.com")
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();
        professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest);

        return bearerTokenForPuiUserManager;
    }

    private void searchUserValidateStatus(String userId, String statusToBeValidated, String orgIdentifier) {

        Map<String, Object> searchResponse = professionalApiClient.searchOrganisationUsersByStatusInternal(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");

        professionalUsersResponses.forEach(user -> {
            if (userId.equalsIgnoreCase((String) user.get("userIdentifier"))) {
                assertThat(user.get("idamStatus").toString()).isEqualTo(statusToBeValidated);
            }
        });
    }
}
