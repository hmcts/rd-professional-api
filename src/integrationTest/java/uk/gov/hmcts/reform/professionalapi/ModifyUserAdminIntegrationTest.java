package uk.gov.hmcts.reform.professionalapi;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.organisationRequestWithAllFields;


@SerenityTest
@Slf4j
class ModifyUserAdminIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    void ac1_modify_administrator_of_active_organisation_with_prd_admin_role_should_return_200() {

        //create org request
        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().build();

        //create org get id
        java.util.Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient
            .createOrganisation(organisationCreationRequest);
        String orgIdentifier = (String) responseForOrganisationCreation.get(ORG_IDENTIFIER);

        updateOrganisationWithGivenRequest(organisationCreationRequest, orgIdentifier, hmctsAdmin, ACTIVE);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        List<String> userRoles = new ArrayList<>();
        userRoles.add(hmctsAdmin);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
            .firstName("someName")
            .lastName("someLastName")
            .email("somenewuser1@email.com")
            .roles(userRoles)
            .build();

        Map<String, Object> newUserResponse =
            professionalReferenceDataClient.addUserToOrganisation(orgIdentifier, userCreationRequest,
                hmctsAdmin);


        //Update Admin
        UserUpdateRequest userUpdateRequest = new UserUpdateRequest(
            "someone@somewhere.com","somenewuser1@email.com");

        Map<String, Object> response1 = professionalReferenceDataClient
                .modifyUserAdminForOrganisation(userUpdateRequest, hmctsAdmin);

        //validate overall response should be 200 always
        assertThat(response1.get("http_status")).isNotNull();
        assertThat(response1.get("http_status")).isEqualTo("200 OK");
        professionalReferenceDataClient.deleteOrganisation(orgIdentifier,hmctsAdmin);

    }


    @Test
    void ac2_modify_administrator_should_return_400_when_up_fails_with_400() {
        //create and update organisation
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);

        Map<String, Object> response = professionalReferenceDataClient
            .modifyUserAdminForOrganisation(null, hmctsAdmin);
        professionalReferenceDataClient.deleteOrganisation(organisationIdentifier,hmctsAdmin);
        //validate overall response should be 200 always
        assertThat(response.get("http_status")).isNotNull();
        assertThat(response.get("http_status")).isEqualTo("400");
        String actualResponseBody = (String) response.get("response_body");
        assertThat(actualResponseBody).containsIgnoringCase("Required request body is missing");

    }


}