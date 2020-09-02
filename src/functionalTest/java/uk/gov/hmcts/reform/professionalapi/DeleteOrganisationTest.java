package uk.gov.hmcts.reform.professionalapi;

import com.microsoft.applicationinsights.boot.dependencies.apachecommons.lang3.RandomStringUtils;
import io.restassured.specification.RequestSpecification;
import java.util.Map;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
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

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createJurisdictions;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
public class DeleteOrganisationTest extends AuthorizationFunctionalTest {

    private boolean deleteOrganisationEnabled;

    @Override
    public void beforeTestClass(TestContext testContext) {
        super.beforeTestClass(testContext);
        String flag = featureToggleService.getLaunchDarklyMap()
            .get("OrganisationInternalController.deleteOrganisation");
        deleteOrganisationEnabled = featureToggleService.isFlagEnabled("rd_professional_api", flag);
    }


    @Test
    public void ac6_could_not_delete_an_active_organisation_with_active_user_profile_by_prd_admin() {
        if (deleteOrganisationEnabled) {
            String firstName = "some-fname";
            String lastName = "some-lname";
            String email = RandomStringUtils.randomAlphabetic(10) + "@usersearch.test".toLowerCase();
            UserCreationRequest superUser = aUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .jurisdictions(createJurisdictions())
                .build();

            bearerToken = professionalApiClient.getMultipleAuthHeadersExternal(puiUserManager, firstName,
                lastName, email);
            OrganisationCreationRequest request = someMinimalOrganisationRequest()
                .superUser(superUser)
                .build();

            Map<String, Object> response = professionalApiClient.createOrganisation(request);
            String orgIdentifier = (String) response.get("organisationIdentifier");
            request.setStatus("ACTIVE");
            professionalApiClient.updateOrganisation(request, hmctsAdmin, orgIdentifier);
            professionalApiClient.deleteOrganisation(orgIdentifier, hmctsAdmin, HttpStatus.BAD_REQUEST);

        }
    }

    @Test
    public void ac7_could_not_delete_an_active_organisation_with_more_than_one_userProfile_by_prdadminSuccessfully() {
        if (false) {
            // create and update organisation
            String orgIdentifierResp = createAndUpdateOrganisationToActive(hmctsAdmin);
            NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserRequest();
            // invite user to the organisation
            Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResp,
                hmctsAdmin, newUserCreationRequest, HttpStatus.CREATED);
            assertThat(newUserResponse).isNotNull();

            professionalApiClient.deleteOrganisation(orgIdentifierResp, hmctsAdmin, HttpStatus.BAD_REQUEST);

        }
    }

    @Test
    public void ac8_could_not_delete_an_active_organisation_with_pending_userProfileByOtherThanPrdAdminThrow403() {
        if (deleteOrganisationEnabled) {
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
            RequestSpecification requestSpecification = professionalApiClient.getMultipleAuthHeadersExternal(
                puiUserManager, firstName, lastName, email);
            professionalApiClient.updateOrganisation(request, hmctsAdmin, orgIdentifier);
            professionalApiClient.deleteOrganisationByExternalUsersBearerToken(orgIdentifier,
                requestSpecification, HttpStatus.FORBIDDEN);

        }
    }
}