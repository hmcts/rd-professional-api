package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.RandomStringUtils;
import io.restassured.specification.RequestSpecification;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestContext;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.CustomSerenityRunner;
import uk.gov.hmcts.reform.professionalapi.util.ToggleEnable;


@RunWith(CustomSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class DeleteOrganisationTest extends AuthorizationFunctionalTest {

    public static final String mapKey = "OrganisationInternalController.deleteOrganisation";

    @Override
    public void beforeTestClass(TestContext testContext) {
        super.beforeTestClass(testContext);
    }

    @Test
    @ToggleEnable(mapKey = mapKey, withFeature = true)
    public void ac1_can_delete_an_organisation_with_valid_org_identifier_by_prd_admin() {

        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifier).isNotEmpty();
        professionalApiClient.deleteOrganisation(orgIdentifier, hmctsAdmin, HttpStatus.NO_CONTENT);
        professionalApiClient.retrieveOrganisationDetails(orgIdentifier, hmctsAdmin, HttpStatus.NOT_FOUND);
    }

    @Test
    @ToggleEnable(mapKey = mapKey, withFeature = true)
    public void ac2_could_throw_not_found_error_when_delete_an_organisation_with_external_endpoint_404() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifier).isNotEmpty();
        professionalApiClient.deleteOrganisationByExternalUser(orgIdentifier, HttpStatus.NOT_FOUND);

    }

    @Test
    @ToggleEnable(mapKey = mapKey, withFeature = true)
    public void ac3_error_when_delete_an_organisation_with_unknown_org_identifier_should_return_404() {
        String orgIdentifier = "C345EDF";
        professionalApiClient.deleteOrganisation(orgIdentifier, hmctsAdmin, HttpStatus.NOT_FOUND);
    }


    @Test
    @ToggleEnable(mapKey = mapKey, withFeature = true)
    public void ac5_delete_an_active_organisation_with_pending_user_profile_by_prd_admin_successfully() {
        String orgIdentifierResponse = createAndUpdateOrganisationToActive(hmctsAdmin);
        professionalApiClient.deleteOrganisation(orgIdentifierResponse, hmctsAdmin, HttpStatus.NO_CONTENT);
        professionalApiClient.retrieveOrganisationDetails(orgIdentifierResponse, hmctsAdmin, HttpStatus.NOT_FOUND);
    }


    @Test
    public void ac6_could_not_delete_an_active_organisation_with_active_user_profile_by_prd_admin() {
        String firstName = "some-fname";
        String lastName = "some-lname";
        String email = RandomStringUtils.randomAlphabetic(10) + "@usersearch.test".toLowerCase();
        UserCreationRequest superUser = aUserCreationRequest()
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .build();

        bearerToken = professionalApiClient.getMultipleAuthHeadersExternalForSuperUser(superUserRoles(), firstName,
            lastName, email);
        OrganisationCreationRequest request = someMinimalOrganisationRequest()
            .superUser(superUser)
            .build();

        Map<String, Object> response = professionalApiClient.createOrganisation(request);
        String orgIdentifier = (String) response.get("organisationIdentifier");
        request.setStatus("ACTIVE");
        log.info("Delete organisation");
        professionalApiClient.updateOrganisation(request, hmctsAdmin, orgIdentifier);
        professionalApiClient.deleteOrganisation(orgIdentifier, hmctsAdmin, HttpStatus.BAD_REQUEST);
    }

    @Test
    @ToggleEnable(mapKey = mapKey, withFeature = true)
    public void ac7_could_not_delete_an_active_organisation_with_more_than_one_userProfile_by_prdadmin_throws_400() {
        // create and update organisation
        String orgIdentifierResp = createAndUpdateOrganisationToActive(hmctsAdmin);
        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
        // invite user to the organisation
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResp,
            hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);
        assertThat(newUserResponse).isNotNull();

        professionalApiClient.deleteOrganisation(orgIdentifierResp, hmctsAdmin, HttpStatus.BAD_REQUEST);
    }

    @Test
    public void ac8_could_not_delete_an_active_organisation_with_pending_userProfileByOtherThanPrdAdminThrow403() {
        String email = randomAlphabetic(10) + "@usersearch.test".toLowerCase();
        String firstName = "some-fname";
        String lastName = "some-lname";
        UserCreationRequest superUser = createSuperUser(email, firstName, lastName);
        OrganisationCreationRequest request = someMinimalOrganisationRequest()
            .superUser(superUser)
            .build();
        Map<String, Object> response = professionalApiClient.createOrganisation(request);
        String orgIdentifier = (String) response.get("organisationIdentifier");
        request.setStatus("ACTIVE");
        log.info("Org response::" + orgIdentifier);

        String idamResponse =
                idamOpenIdClient.getExternalOpenIdToken("pui-user-manager", firstName, lastName, email);

        RequestSpecification requestSpecification = professionalApiClient.getMultipleAuthHeaders(idamResponse);

        professionalApiClient.updateOrganisation(request, hmctsAdmin, orgIdentifier);
        log.info("Org response::" + orgIdentifier);
        professionalApiClient.deleteOrganisationByExternalUsersBearerToken(orgIdentifier,
            requestSpecification, HttpStatus.FORBIDDEN);
    }
}