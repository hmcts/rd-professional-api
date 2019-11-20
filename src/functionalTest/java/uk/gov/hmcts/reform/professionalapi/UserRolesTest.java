package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class UserRolesTest extends AuthorizationFunctionalTest {

    private String orgIdentifier;

    @Test
    public void ac1_super_user_can_have_fpla_or_iac_roles() {

        String email = randomAlphabetic(10) + "@usersearch.test".toLowerCase();
        OrganisationCreationRequest request = someMinimalOrganisationRequest()
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email(email)
                        .jurisdictions(OrganisationFixtures.createJurisdictions())
                        .build())
                .build();

        Map<String, Object> response = professionalApiClient.createOrganisation(request);
        orgIdentifier = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);

        //response = professionalApiClient.retrieveOrganisationDetails(orgIdentifier, puiUserManager);
        //log.info("ORG DETAILS RESPONSE::::::::::" + response);

        Map<String, Object> searchResponse = professionalApiClient.searchForUserByEmailAddress(email.toLowerCase(), hmctsAdmin);
        log.info("USER EMAIL SEARCH RESPONSE::::::::::::" + searchResponse);

        List<String> userRoles = (List<String>) searchResponse.get("roles");
        log.info("USER ROLES::::::::::::" + userRoles);

        assertThat(userRoles).contains("caseworker-publiclaw", "caseworker-publiclaw-solicitor", "caseworker-ia-legalrep-solicitor");
    }
}
