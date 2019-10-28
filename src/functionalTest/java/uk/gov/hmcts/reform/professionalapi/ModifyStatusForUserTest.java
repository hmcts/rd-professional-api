package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserProfileData;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;



@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class ModifyStatusForUserTest extends AuthorizationFunctionalTest {

    @Test
    public void add_new_user_to_organisation() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();

        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        assertThat(newUserCreationRequest).isNotNull();

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin,newUserCreationRequest);

        assertThat(newUserResponse).isNotNull();

        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();
        RoleName role1 = new RoleName("pui-user-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        //modifyUserProfileData.setIdamStatus(IdamStatus.ACTIVE.name());
        modifyUserProfileData.setRolesAdd(roles);

        String userId = (String) newUserResponse.get("userIdentifier");

        HttpStatus httpStatus = HttpStatus.OK;


        /*Map<String, Object> actualData = */professionalApiClient.modifyUserToExistingUserForPrdAdmin(httpStatus, modifyUserProfileData, orgIdentifierResponse, userId);

        /*log.info("RDCC-418::actualData: " + actualData.keySet());
        log.info("@@@@@@@@EMAIL: " + modifyUserProfileData.getEmail());

        assertThat(actualData).isNotNull();
        assertThat(actualData.keySet().size() > 0).isTrue();*/

        String status = searchUserStatus(orgIdentifierResponse, userId);
        log.info("@@@@@@@@@@@@@status:" + status);

        assertThat(StringUtils.isNotBlank(status)).isTrue();
        //assertThat(status).isEqualTo("SUSPENDED");

        /*
            public Map<String,Object> modifyUserToExistingUserForPrdAdmin(HttpStatus status, ModifyUserProfileData modifyUserProfileData, String organisationId, String userId) {

        Response response = getMultipleAuthHeadersInternal()
                .body(modifyUserProfileData)
                .put("/refdata/internal/v1/organisations/" + organisationId + "/users/" + userId)
                .andReturn();
         */

    }


    private String searchUserStatus(String orgIdentifier, String userId) {

        Map<String, Object> searchResponse = professionalApiClient.searchOrganisationUsersByStatusInternal(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");

        return professionalUsersResponses.stream()
                .filter(user -> ((String) user.get("userIdentifier")).equalsIgnoreCase(userId))
                .map(user -> (String) user.get("idamStatus"))
                .collect(Collectors.toList()).get(0);
    }


}
