package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class ModifyStatusForUserTest extends AuthorizationFunctionalTest {

    @Test
    public void rdcc_418_ac1_update_user_status_from_suspended_to_active() {
        IdamOpenIdClient idamOpenIdClient = new IdamOpenIdClient(configProperties);
        Map<String,String> userCreds = idamOpenIdClient.createUser(addRoles("pui-organisation-manager"));
        NewUserCreationRequest newUserCreationRequest = professionalApiClient
                .createNewUserRequest(userCreds.get(EMAIL));

        assertThat(newUserCreationRequest).isNotNull();

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(activeOrgId,
                hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);

        assertThat(newUserResponse).isNotNull();

        String userId = (String) newUserResponse.get("userIdentifier");

        UserProfileUpdatedData data = getUserStatusUpdateRequest(IdamStatus.SUSPENDED);

        Map<String,Object> modifyStatusResponse = professionalApiClient
                .modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, data, activeOrgId, userId);

        String status = searchUserStatus(activeOrgId, userId);

        assertThat(status).isEqualTo(IdamStatus.SUSPENDED.name());

        data = getUserStatusUpdateRequest(IdamStatus.ACTIVE);

        professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, data, activeOrgId, userId);

        status = searchUserStatus(activeOrgId, userId);

        assertThat(status).isEqualTo(IdamStatus.ACTIVE.name());


    }

    @Test
    public void rdcc_418_ac2_update_user_status_from_active_to_suspended() {
        IdamOpenIdClient idamOpenIdClient = new IdamOpenIdClient(configProperties);
        Map<String,String> userCreds = idamOpenIdClient.createUser(addRoles("pui-organisation-manager"));
        NewUserCreationRequest newUserCreationRequest = professionalApiClient
                .createNewUserRequest(userCreds.get(EMAIL));

        assertThat(newUserCreationRequest).isNotNull();

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(activeOrgId,
                hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);

        assertThat(newUserResponse).isNotNull();

        UserProfileUpdatedData data = getUserStatusUpdateRequest(IdamStatus.SUSPENDED);

        String userId = (String) newUserResponse.get("userIdentifier");

        professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, data, activeOrgId, userId);

        String status = searchUserStatus(activeOrgId, userId);

        assertThat(status).isEqualTo(IdamStatus.SUSPENDED.name());
    }

    @SuppressWarnings("unchecked")
    private String searchUserStatus(String orgIdentifier, String userId) {

        Map<String, Object> searchResponse = professionalApiClient
                .searchOrganisationUsersByStatusInternal(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");

        return professionalUsersResponses.stream()
                .filter(user -> ((String) user.get("userIdentifier")).equalsIgnoreCase(userId))
                .map(user -> (String) user.get("idamStatus"))
                .collect(Collectors.toList()).get(0);
    }
}