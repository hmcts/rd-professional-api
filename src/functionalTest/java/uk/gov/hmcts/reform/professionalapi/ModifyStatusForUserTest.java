package uk.gov.hmcts.reform.professionalapi;

import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserProfileData;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.idam.IdamOpenIdClient;
import uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures;

import java.util.*;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;

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

        HttpStatus httpStatus = HttpStatus.OK;

        ModifyUserProfileData modifyUserProfileData = new ModifyUserProfileData();
        RoleName role1 = new RoleName("pui-user-manager");
        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        modifyUserProfileData.setIdamStatus(IdamStatus.SUSPENDED.name());
        modifyUserProfileData.setRolesAdd(roles);

        String userId = (String) newUserResponse.get("userIdentifier");

        Map<String, Object> actualData = professionalApiClient.modifyUserToExistingUserForPrdAdmin(httpStatus, modifyUserProfileData, orgIdentifierResponse, userId);

        log.info("RDCC-418::actualData" + actualData.keySet());

        assertThat(actualData).isNotNull();

        /*

            public Map<String,Object> modifyUserToExistingUserForPrdAdmin(HttpStatus status, ModifyUserProfileData modifyUserProfileData, String organisationId, String userId) {

        Response response = getMultipleAuthHeadersInternal()
                .body(modifyUserProfileData)
                .put("/refdata/internal/v1/organisations/" + organisationId + "/users/" + userId)
                .andReturn();


         */
    }


}
