package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;

import java.util.*;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;
import org.springframework.http.HttpStatus;

import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.RoleName;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures;


@Slf4j
public class ModifyUserStatusIntegrationTest extends AuthorizationEnabledIntegrationTest {

    @Test
    public void ac1_modify_status_of_active_user_for_an_active_organisation_with_prd_admin_role_should_return_200() {

        String organisationIdentifier = createOrganisationRequest();
        updateOrganisation(organisationIdentifier, hmctsAdmin, ACTIVE);
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        List<String> userRoles = new ArrayList<>();
        userRoles.add(puiCaseManager);

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        updateUserProfileRolesMock(HttpStatus.OK);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName("someName")
                .lastName("someLastName")
                .email(randomAlphabetic(5) + "@email.com")
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();

        Map<String, Object> newUserResponse =
                professionalReferenceDataClient.addUserToOrganisation(organisationIdentifier, userCreationRequest, hmctsAdmin);

        String userIdentifier = (String) newUserResponse.get("userIdentifier");
        UserProfileUpdatedData userProfileUpdatedData = createModifyUserProfileData();
        userProfileUpdatedData.setIdamStatus("SUSPENDED");

        updateUserProfileAddRolesMock(HttpStatus.OK);
        Map<String, Object> response = professionalReferenceDataClient.modifyUserRolesOfOrganisation(userProfileUpdatedData, organisationIdentifier, userIdentifier, hmctsAdmin);
        assertThat(response.get("http_status")).isNotNull();
        assertThat(response.get("http_status")).isEqualTo("200 OK");
        Map<String, Object> addRolesResponse = ((Map<String, Object>) response.get("updateStatusResponse"));
        assertThat(addRolesResponse.get("idamStatusCode")).isEqualTo("200");
        assertThat(addRolesResponse.get("idamMessage")).isEqualTo("Success");

    }

    private UserProfileUpdatedData createModifyUserProfileData() {

        UserProfileUpdatedData userProfileUpdatedData = new UserProfileUpdatedData();
        RoleName roleName1 = new RoleName(puiCaseManager);
        RoleName roleName2 = new RoleName(puiOrgManager);
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        userProfileUpdatedData.setRolesAdd(roles);
        return userProfileUpdatedData;
    }
}