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

        modifyUserProfileData.setIdamStatus(IdamStatus.SUSPENDED.name());

        String userId = (String) newUserResponse.get("userIdentifier");

        HttpStatus httpStatus = HttpStatus.OK;

        professionalApiClient.modifyUserToExistingUserForPrdAdmin(httpStatus, modifyUserProfileData, orgIdentifierResponse, userId);

        String status = searchUserStatus(orgIdentifierResponse, userId);
        log.info("@@@@@@@@@@@@@status:" + status);

        assertThat(StringUtils.isNotBlank(status)).isTrue();
        assertThat(status).isEqualTo("SUSPENDED");

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
