package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.Map;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Ignore
public class UserEmailSearchTest extends FunctionalTestSuite {

    @Test
    public void can_find_a_user_by_their_email_address() {

        String email = randomAlphabetic(10) + "@usersearch.test";
        OrganisationCreationRequest request = someMinimalOrganisationRequest()
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email(email)
                        .build())
                .build();
        Map<String, Object> response = professionalApiClient.createOrganisation(request);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();
        professionalApiClient.updateOrganisation(orgIdentifierResponse);

        Map<String, Object> searchResponse = professionalApiClient.searchForUserByEmailAddress(email);

        assertThat(searchResponse.get("firstName")).isEqualTo("some-fname");
    }
}