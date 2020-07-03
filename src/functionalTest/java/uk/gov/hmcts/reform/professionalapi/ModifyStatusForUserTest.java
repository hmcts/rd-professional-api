package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;


@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class ModifyStatusForUserTest extends AuthorizationFunctionalTest {

    @Test
    public void rdcc_418_ac1_update_user_status_from_suspended_to_active() {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);


        IdamOpenIdClient idamOpenIdClient = new IdamOpenIdClient(configProperties);
        String email = idamOpenIdClient.createUser("pui-organisation-manager");
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest(email);

        assertThat(newUserCreationRequest).isNotNull();

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier,
                hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);

        assertThat(newUserResponse).isNotNull();


        String userId = (String) newUserResponse.get("userIdentifier");

        UserProfileUpdatedData data = new UserProfileUpdatedData();

        data.setFirstName("UpdatedFirstName");
        data.setLastName("UpdatedLastName");
        data.setIdamStatus(IdamStatus.SUSPENDED.name());

        Map<String,Object> modifyStatusResponse = professionalApiClient
                .modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, data, orgIdentifier, userId);

        String status = searchUserStatus(orgIdentifier, userId);

        assertThat(status).isEqualTo(IdamStatus.SUSPENDED.name());

        data.setFirstName("UpdatedFirstName");
        data.setLastName("UpdatedLastName");
        data.setIdamStatus(IdamStatus.ACTIVE.name());

        professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, data, orgIdentifier, userId);

        status = searchUserStatus(orgIdentifier, userId);

        assertThat(status).isEqualTo(IdamStatus.ACTIVE.name());


    }

    @Test
    public void rdcc_418_ac2_update_user_status_from_active_to_suspended() {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);


        IdamOpenIdClient idamOpenIdClient = new IdamOpenIdClient(configProperties);
        String email = idamOpenIdClient.createUser("pui-organisation-manager");
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest(email);

        assertThat(newUserCreationRequest).isNotNull();

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier,
                hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);

        assertThat(newUserResponse).isNotNull();

        UserProfileUpdatedData data = new UserProfileUpdatedData();

        data.setFirstName("UpdatedFirstName");
        data.setLastName("UpdatedLastName");
        data.setIdamStatus(IdamStatus.SUSPENDED.name());

        String userId = (String) newUserResponse.get("userIdentifier");

        professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, data, orgIdentifier, userId);

        String status = searchUserStatus(orgIdentifier, userId);

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
