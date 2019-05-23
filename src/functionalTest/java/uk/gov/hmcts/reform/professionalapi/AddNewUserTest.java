package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;


@RunWith(SpringRunner.class)
@ActiveProfiles("functional")
public class AddNewUserTest extends FunctionalTestSuite {

    @Test
    public void add_new_user_to_organisation() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();

        NewUserCreationRequest newUserCreationRequest = professionalApiClient.createNewUserCreationRequest();
        assertThat(newUserCreationRequest).isNotNull();
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, newUserCreationRequest);

        assertThat(newUserResponse).isNotNull();
    }
}
