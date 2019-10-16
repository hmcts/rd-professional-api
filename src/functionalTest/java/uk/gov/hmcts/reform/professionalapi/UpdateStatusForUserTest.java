package uk.gov.hmcts.reform.professionalapi;

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

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class UpdateStatusForUserTest extends ModifyRolesForUserTest {

    @Test
    public void ac1_modify_status_existing_user_from_ACTIVE_to_PENDING() {

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

        Map<String, Object> searchResponse = professionalApiClient.searchAllActiveUsersByOrganisation(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");
        Map professionalUsersResponse = professionalUsersResponses.get(1);

        String userId = (String) professionalUsersResponse.get("userIdentifier");
        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();
        modifyUserProfileData.setIdamStatus(IdamStatus.SUSPENDED.toString());

        professionalApiClient.modifyUserStatusToExistingUserForPrdAdmin(HttpStatus.OK, modifyUserProfileData, orgIdentifier, userId);

        searchUserValidateStatus(userId, "SUSPENDED", orgIdentifier);

    }

    private void searchUserValidateStatus(String userId, String statusToBeValidated, String orgIdentifier) {

        Map<String, Object> searchResponse = professionalApiClient.searchAllActiveUsersByOrganisation(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");

        professionalUsersResponses.forEach(user -> {
            if (userId.equalsIgnoreCase((String) user.get("userIdentifier"))) {
                assertThat(user.get("idamStatus").toString()).isEqualTo(statusToBeValidated);
            }
        });
    }
}
