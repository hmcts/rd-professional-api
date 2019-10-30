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
    public void rdcc_418_update_user_status() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();

        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        assertThat(newUserCreationRequest).isNotNull();

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, newUserCreationRequest);

        assertThat(newUserResponse).isNotNull();

        ModifyUserProfileData data = new ModifyUserProfileData();

        data.setEmail(newUserCreationRequest.getEmail());
        data.setFirstName(newUserCreationRequest.getFirstName());
        data.setLastName(newUserCreationRequest.getLastName());
        data.setIdamStatus(IdamStatus.ACTIVE.name());

        Set<RoleName> rolesAdd = new HashSet<>();
        RoleName rn = new RoleName("pui-case-manager");
        rolesAdd.add(rn);
        data.setRolesAdd(rolesAdd);

        Set<RoleName> rolesDelete = new HashSet<>();
        data.setRolesDelete(rolesDelete);

        String userId = (String) newUserResponse.get("userIdentifier");

        HttpStatus httpStatus = HttpStatus.OK;

        professionalApiClient.modifyUserToExistingUserForPrdAdmin(httpStatus, data, orgIdentifierResponse, userId);

        String status = searchUserStatus(orgIdentifierResponse, userId);
        log.info("@@@@@@@@@@@@@status:" + status);

        assertThat(StringUtils.isNotBlank(status)).isTrue();
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
