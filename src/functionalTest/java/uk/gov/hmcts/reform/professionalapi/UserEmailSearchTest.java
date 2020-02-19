package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.Map;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
public class UserEmailSearchTest extends AuthorizationFunctionalTest {


    @Test
    public void can_find_a_user_by_their_email_address() {

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

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();
        request.setStatus("ACTIVE");
        professionalApiClient.updateOrganisation(request, hmctsAdmin, orgIdentifierResponse);

        Map<String, Object> searchResponse = professionalApiClient.searchForUserByEmailAddress(email.toLowerCase(), hmctsAdmin);

        assertThat(searchResponse.get("firstName")).isEqualTo("some-fname");
    }

    @Test
    public void can_search_by_email_regardless_of_case() {

        String emailIgnoreCase = randomAlphabetic(10) + "@usersearch.test".toUpperCase();
        OrganisationCreationRequest request = someMinimalOrganisationRequest()
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email(emailIgnoreCase)
                        .jurisdictions(OrganisationFixtures.createJurisdictions())
                        .build())
                .build();
        Map<String, Object> response = professionalApiClient.createOrganisation(request);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        request.setStatus("ACTIVE");
        professionalApiClient.updateOrganisation(request, hmctsAdmin, orgIdentifierResponse);

        Map<String, Object> searchResponse = professionalApiClient.searchForUserByEmailAddress(emailIgnoreCase, hmctsAdmin);
        assertThat(searchResponse.get("email")).isEqualTo(emailIgnoreCase.toLowerCase());
    }
}