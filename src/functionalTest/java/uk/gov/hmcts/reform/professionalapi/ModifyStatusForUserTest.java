package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;


@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
@Ignore
public class ModifyStatusForUserTest extends AuthorizationFunctionalTest {

    @Test
    public void rdcc_418_ac1_update_user_status_from_active_to_suspended() {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        NewUserCreationRequest userCreationRequest = professionalApiClient.createNewUserRequest();
        assertThat(userCreationRequest).isNotNull();

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, userCreationRequest, HttpStatus.OK);
        assertThat(newUserResponse).isNotNull();

        String userId = (String) newUserResponse.get("userIdentifier");

        UserProfileUpdatedData data = new UserProfileUpdatedData();

        data.setFirstName("UpdatedFirstName");
        data.setLastName("UpdatedLastName");
        data.setIdamStatus(IdamStatus.SUSPENDED.name());

        Map<String,Object> modifyStatusResponse = professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, data, orgIdentifier, userId);

        String status = searchUserStatus(orgIdentifier, userId);

        assertThat(status).isEqualTo(IdamStatus.SUSPENDED.name());
    }

    @Test
    public void rdcc_418_ac2_update_user_status_from_active_to_suspended_and_up_fails() {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        NewUserCreationRequest userCreationRequest = professionalApiClient.createNewUserRequest();
        assertThat(userCreationRequest).isNotNull();
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, userCreationRequest, HttpStatus.OK);
        assertThat(newUserResponse).isNotNull();

        String userId = (String) newUserResponse.get("userIdentifier");

        UserProfileUpdatedData data = new UserProfileUpdatedData();

        data.setFirstName("UpdatedFirstName");
        data.setLastName("UpdatedLastName");
        data.setIdamStatus(IdamStatus.SUSPENDED.name());

        Map<String,Object> modifyStatusResponse = professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, data, orgIdentifier, userId);

        String status = searchUserStatus(orgIdentifier, userId);

        assertThat(status).isEqualTo(IdamStatus.SUSPENDED.name());
    }

    @SuppressWarnings("unchecked")
    private String searchUserStatus(String orgIdentifier, String userId) {

        Map<String, Object> searchResponse = professionalApiClient.searchOrganisationUsersByStatusInternal(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");

        return professionalUsersResponses.stream()
                .filter(user -> ((String) user.get("userIdentifier")).equalsIgnoreCase(userId))
                .map(user -> (String) user.get("idamStatus"))
                .collect(Collectors.toList()).get(0);
    }


}
