package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("functional")
public class OrganisationCreationsTest extends FunctionalTestSuite {

    @Test
    public void can_create_an_organisation() {

        String organisationName = randomAlphabetic(10);
        String[] pbas = new String[] { randomAlphabetic(10), randomAlphabetic(10) };

        Map<String, Object> response = professionalApiClient.createOrganisation(organisationName, pbas);

        assertThat(response.get("name")).isEqualTo(organisationName);
        assertThat(userIdsFrom(response).size()).isEqualTo(1);
        assertThat(paymentAccountsFrom(response).size()).isEqualTo(2);
    }

    @SuppressWarnings("unchecked")
    private List<String> userIdsFrom(Map<String, Object> response) {
        return (List<String>) response.get("userIds");
    }

    @SuppressWarnings("unchecked")
    private List<String> paymentAccountsFrom(Map<String, Object> response) {
        return (List<String>) response.get("pbaAccounts");
    }

}