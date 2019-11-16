package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;

import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures;


@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class FindUsersStatusByEmailTest extends AuthorizationFunctionalTest {

    //RequestSpecification bearerTokenForPuiUserManager;
    RequestSpecification bearerTokenForNonPuiUserManager;


    public RequestSpecification generateBearerTokenForPuiManager() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        bearerTokenForPuiUserManager = professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, userEmail);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();
        professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest);

        return bearerTokenForPuiUserManager;
    }

    public RequestSpecification generateBearerTokenForNonPuiManager() {
        if (bearerTokenForNonPuiUserManager == null) {

            Map<String, Object> response = professionalApiClient.createOrganisation();
            String orgIdentifierResponse = (String) response.get("organisationIdentifier");
            professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

            List<String> userRoles = new ArrayList<>();
            userRoles.add("pui-case-manager");
            String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
            String lastName = "someLastName";
            String firstName = "someName";

            bearerTokenForNonPuiUserManager = professionalApiClient.getMultipleAuthHeadersExternal(puiCaseManager, firstName, lastName, userEmail);

            NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                    .firstName(firstName)
                    .lastName(lastName)
                    .email(userEmail)
                    .roles(userRoles)
                    .jurisdictions(OrganisationFixtures.createJurisdictions())
                    .build();
            professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest);

            return bearerTokenForNonPuiUserManager;
        } else {
            return bearerTokenForNonPuiUserManager;
        }
    }


    @Test
    public void ac1_find_user_status_by_email_with_non_pui_user_manager_role_should_return_200() {
        String orgId =  createAndUpdateOrganisationToActive(hmctsAdmin);
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-case-manager");
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someFirstName";
        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();
        professionalApiClient.addNewUserToAnOrganisation(orgId, hmctsAdmin, userCreationRequest);
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK, generateBearerTokenForNonPuiManager(), "Active");

    }



    @Test
    public void ac2_find_user_status_by_email_with_non_pui_user_manager_role_should_return_200() {
        Map<String, Object> response = professionalApiClient.findUserStatusByEmail(HttpStatus.OK, generateBearerTokenForPuiManager(), "");

    }

}
