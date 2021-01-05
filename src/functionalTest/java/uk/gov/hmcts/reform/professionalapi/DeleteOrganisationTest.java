package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestContext;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;


@RunWith(SpringIntegrationSerenityRunner.class)
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
    @Ignore("convert to integration test once RDCC-2050 is completed")
    //@ToggleEnable(mapKey = mapKey, withFeature = true)
    public void ac2_could_throw_not_found_error_when_delete_an_organisation_with_external_endpoint_404() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifier = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifier).isNotEmpty();
        professionalApiClient.deleteOrganisationByExternalUser(orgIdentifier, HttpStatus.NOT_FOUND);

    }

    @Ignore("convert to integration test once RDCC-2050 is completed")
    @Test
    //@ToggleEnable(mapKey = mapKey, withFeature = true)
    public void ac3_error_when_delete_an_organisation_with_unknown_org_identifier_should_return_404() {
        String orgIdentifier = "C345EDF";
        professionalApiClient.deleteOrganisation(orgIdentifier, hmctsAdmin, HttpStatus.NOT_FOUND);
    }


    @Ignore("convert to integration test once RDCC-2050 is completed")
    @Test
    public void ac6_could_not_delete_an_active_organisation_with_active_user_profile_by_prd_admin() {
        String firstName = "some-fname";
        String lastName = "some-lname";
        String email = generateRandomEmail().toLowerCase();

        email = getExternalSuperUserTokenWithRetry(email, firstName, lastName);

        UserCreationRequest superUser = aUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .build();

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

    @Ignore("convert to integration test once RDCC-2050 is completed")
    @Test
    //@ToggleEnable(mapKey = mapKey, withFeature = true)
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
}