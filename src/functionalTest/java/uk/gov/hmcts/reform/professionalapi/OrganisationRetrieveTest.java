package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("functional")
public class OrganisationRetrieveTest extends FunctionalTestSuite {


    @Test
    public void can_retrieve_an_organisation() {

        String organisationName = randomAlphabetic(10);
        String[] paymentNumbers = new String[]{randomAlphabetic(10), randomAlphabetic(10)};
        Map<String, Object> response = professionalApiClient.retrieveOrganisationDetails();
        assertThat(response.get("organisations")).isNotNull();
        Assertions.assertThat(response.size()).isEqualTo(1);
    }

}
