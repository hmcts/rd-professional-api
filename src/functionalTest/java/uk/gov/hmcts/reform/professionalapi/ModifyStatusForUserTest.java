package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createJurisdictions;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;


@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class ModifyStatusForUserTest extends AuthorizationFunctionalTest {

    @Before
    public void createAndUpdateOrganisation() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        // creating user in idam with the same email used to create Organisation so that status is already Active in UP
        bearerToken = professionalApiClient.getMultipleAuthHeadersExternal(hmctsAdmin, firstName, lastName, userEmail);

        //create organisation with the same Super User Email
        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().superUser(aUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .jurisdictions(createJurisdictions())
                .build()).build();

        Map<String, Object> response = professionalApiClient.createOrganisation(organisationCreationRequest);
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);
    }

    @Test
    public void rdcc_418_ac1_update_user_status_from_suspended_to_active() {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);


        IdamOpenIdClient idamOpenIdClient = new IdamOpenIdClient(configProperties);
        String email = idamOpenIdClient.createUser("pui-organisation-manager");
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest(email);

        assertThat(newUserCreationRequest).isNotNull();

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED, bearerToken);

        assertThat(newUserResponse).isNotNull();


        String userId = (String) newUserResponse.get("userIdentifier");

        UserProfileUpdatedData data = new UserProfileUpdatedData();

        data.setFirstName("UpdatedFirstName");
        data.setLastName("UpdatedLastName");
        data.setIdamStatus(IdamStatus.SUSPENDED.name());

        Map<String,Object> modifyStatusResponse = professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, data, orgIdentifier, userId);

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

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED, bearerToken);

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

        Map<String, Object> searchResponse = professionalApiClient.searchOrganisationUsersByStatusInternal(orgIdentifier, hmctsAdmin, HttpStatus.OK);
        List<Map> professionalUsersResponses = (List<Map>) searchResponse.get("users");

        return professionalUsersResponses.stream()
                .filter(user -> ((String) user.get("userIdentifier")).equalsIgnoreCase(userId))
                .map(user -> (String) user.get("idamStatus"))
                .collect(Collectors.toList()).get(0);
    }


}
