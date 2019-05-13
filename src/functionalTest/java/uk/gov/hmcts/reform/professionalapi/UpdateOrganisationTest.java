package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("functional")
public class UpdateOrganisationTest extends FunctionalTestSuite {


    @Test
    public void can_update_an_organisation() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();
        professionalApiClient.updateOrganisation(orgIdentifierResponse);
    }
}
