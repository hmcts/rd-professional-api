package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.getNestedValue;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class AddNewUserTest extends AuthorizationFunctionalTest {

    static String orgIdentifierResponse = null;

    @Before
    public void createAndUpdateOrganisation() {
        if (isEmpty(orgIdentifierResponse)) {
            orgIdentifierResponse = createAndUpdateOrganisationToActive(hmctsAdmin);
        }
    }

    @Ignore("convert to integration test once RDCC-2050 is completed")
    @Test
    public void add_new_user_with_caa_roles_to_organisation_should_return_201() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add(puiCaa);
        userRoles.add(caseworkerCaa);
        userRoles.add(puiUserManager);
        String firstName = "someName";
        String lastName = "someLastName";
        String email = generateRandomEmail().toLowerCase();

        NewUserCreationRequest newUserCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .roles(userRoles)
                .build();

        professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName, lastName, email);

        Map<String, Object> newUserResponse = professionalApiClient
                .addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, newUserCreationRequest,
                        HttpStatus.CREATED);
        assertThat(newUserResponse).isNotNull();

        Map<String, Object> searchUserResponse = professionalApiClient
                .searchUsersByOrganisation(orgIdentifierResponse, hmctsAdmin, "false", HttpStatus.OK,
                        "true");

        List<Map> users = getNestedValue(searchUserResponse, "users");
        Map newUserDetails = getActiveUser(users);
        List<String> superUserRoles = getNestedValue(newUserDetails, "roles");

        assertThat(superUserRoles).contains(puiCaa, caseworkerCaa);
    }
}