package uk.gov.hmcts.reform.professionalapi;

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
    public void can_retrieve_all_organisations() {
        professionalApiClient.createOrganisation();

        Map<String, Object> response = professionalApiClient.retrieveAllOrganisations();
        assertThat(response.get("organisations")).isNotNull();
        Assertions.assertThat(response.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void can_retrieve_a_single_organisation() {
        Map<String, Object> response = professionalApiClient.createOrganisation();

        response = professionalApiClient.retrieveOrganisationDetails((String) response.get("organisationIdentifier"));
        assertThat(response.get("name")).isNotNull();
        assertThat(response.get("status")).isEqualTo("PENDING");
        assertThat(response.get("sraId")).isNotNull();
        assertThat(response.get("sraRegulated")).isNotNull();
        assertThat(response.get("companyNumber")).isNotNull();
        assertThat(response.get("companyUrl")).isNotNull();
        assertThat(response.get("superUser")).isNotNull();
        assertThat(response.get("pbaAccounts")).isNotNull();
        assertThat(response.get("contactInformation")).isNotNull();
        Assertions.assertThat(response.size()).isGreaterThanOrEqualTo(1);
    }

}
