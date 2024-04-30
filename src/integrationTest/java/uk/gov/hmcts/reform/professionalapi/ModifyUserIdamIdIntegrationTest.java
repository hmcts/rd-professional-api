package uk.gov.hmcts.reform.professionalapi;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;
import uk.gov.hmcts.reform.professionalapi.controller.request.ProfessionalUserIdentifierRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;

@SerenityTest
@Slf4j
class ModifyUserIdamIdIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    void ac1_modify_idamId_of_active_user_for_an_active_organisation_should_return_200() {
        //create and update organisation
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);

        //create new user
        List<String> userRoles = new ArrayList<>();
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        userRoles.add(puiCaseManager);

        Map<String, Object> newUserResponse =
            professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier,
                inviteUserCreationRequest(randomAlphabetic(5) + "@email.com",
                    userRoles), hmctsAdmin);
        String existingUserIdentifier = (String) newUserResponse.get(USER_IDENTIFIER);

        ProfessionalUserIdentifierRequest professionalUserIdentifierRequest =  ProfessionalUserIdentifierRequest
            .aUserIdentifierRequest().existingIdamId(existingUserIdentifier).newIdamId(UUID.randomUUID().toString())
            .build();

        //modify Idam  details in user Profile
        userProfileCreateUserWireMock(HttpStatus.CREATED);

        updateUserProfileRolesMock(HttpStatus.OK);

        Map<String, Object> modifiedUserResponse =
            professionalReferenceDataClient.updateUserIdamForOrganisation(professionalUserIdentifierRequest,hmctsAdmin);


        //validate overall response should be 200 always
        assertThat(modifiedUserResponse.get("http_status")).isNotNull();
        assertThat(modifiedUserResponse.get("http_status")).isEqualTo("200 OK");

    }


    @Test
    void ac2_modify_idam_newId_null_should_return_400() {

        //create and update organisation
        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);

        //create new user
        List<String> userRoles = new ArrayList<>();
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        userRoles.add(puiCaseManager);

        Map<String, Object> newUserResponse =
            professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier,
                inviteUserCreationRequest(randomAlphabetic(5) + "@email.com",
                    userRoles), hmctsAdmin);
        String existingUserIdentifier = (String) newUserResponse.get(USER_IDENTIFIER);

        ProfessionalUserIdentifierRequest professionalUserIdentifierRequest =  ProfessionalUserIdentifierRequest
            .aUserIdentifierRequest().existingIdamId(existingUserIdentifier).newIdamId(null)
            .build();

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        updateUserProfileRolesMock(HttpStatus.BAD_REQUEST);

        Map<String, Object> modifiedUserResponse =
            professionalReferenceDataClient.updateUserIdamForOrganisation(professionalUserIdentifierRequest,hmctsAdmin);

        assertThat(modifiedUserResponse.get("http_status")).isNotNull();
        assertThat(modifiedUserResponse).containsEntry("http_status","400");

    }


    @Test
    void ac2_modify_idam_existingId_null_should_return_400() {

        ProfessionalUserIdentifierRequest professionalUserIdentifierRequest =  ProfessionalUserIdentifierRequest
            .aUserIdentifierRequest().existingIdamId(null).newIdamId(UUID.randomUUID().toString())
            .build();

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        updateUserProfileRolesMock(HttpStatus.BAD_REQUEST);

        Map<String, Object> modifiedUserResponse =
            professionalReferenceDataClient.updateUserIdamForOrganisation(professionalUserIdentifierRequest,hmctsAdmin);

        assertThat(modifiedUserResponse.get("http_status")).isNotNull();
        assertThat(modifiedUserResponse).containsEntry("http_status","400");
    }
}