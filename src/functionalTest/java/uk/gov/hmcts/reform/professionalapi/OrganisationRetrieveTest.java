package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

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

    @Test
    public void can_retrieve_an_organisation_by_request_param_status_equal_to_pending() {

        String organisationName = randomAlphabetic(10);
        String[] paymentNumbers = new String[]{randomAlphabetic(10), randomAlphabetic(10)};
        Map<String, Object> response = professionalApiClient
                .retrieveOrganisationDetailsByStatus(OrganisationStatus.PENDING.getStatus().toUpperCase());
        assertThat(response.get("organisations")).asList().isNotEmpty();
        assertThat(response.size()).isEqualTo(1);
    }

    @Test
    public void can_retrieve_an_organisation_by_request_param_status_equal_to_active() {

        String organisationName = randomAlphabetic(10);
        String[] paymentNumbers = new String[]{randomAlphabetic(10), randomAlphabetic(10)};
        Map<String, Object> response = professionalApiClient
                .retrieveOrganisationDetailsByStatus(OrganisationStatus.ACTIVE.getStatus().toUpperCase());
        assertThat(response.get("organisations")).isNotNull();
        assertThat(response.size()).isEqualTo(1);
    }

    @Test
    public void can_retrieve_400_error_code_by_request_param_status_value_other_than_required_values() {

        String organisationName = randomAlphabetic(10);
        String[] paymentNumbers = new String[]{randomAlphabetic(10), randomAlphabetic(10)};
        professionalApiClient
                .retrieveOrganisationDetailsByUnknownStatus("ACTIV");
    }

}
