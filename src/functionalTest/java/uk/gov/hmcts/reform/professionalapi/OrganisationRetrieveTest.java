package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@RunWith(SpringIntegrationSerenityRunner.class)
@Slf4j
@Ignore
public class OrganisationRetrieveTest extends AuthorizationFunctionalTest {

    @Test
    public void can_retrieve_all_organisations() {
        professionalApiClient.createOrganisation();

        Map<String, Object> response = professionalApiClient.retrieveAllOrganisations(puiCaseManager);
        assertThat(response.get("organisations")).isNotNull();
        Assertions.assertThat(response.size()).isGreaterThanOrEqualTo(1);
    }

    @Ignore
    @Test
    public void can_retrieve_a_single_organisation() {
        Map<String, Object> response = professionalApiClient.createOrganisation();

        response = professionalApiClient.retrieveOrganisationDetails((String) response.get("organisationIdentifier"),puiCaseManager);
        assertThat(response.get("name")).isNotNull();
        assertThat(response.get("status")).isEqualTo("PENDING");
        assertThat(response.get("sraId")).isNotNull();
        assertThat(response.get("sraRegulated")).isNotNull();
        assertThat(response.get("companyNumber")).isNotNull();
        assertThat(response.get("companyUrl")).isNotNull();
        assertThat(response.get("superUser")).isNotNull();
        assertThat(response.get("paymentAccount")).isNotNull();
        assertThat(response.get("contactInformation")).isNotNull();
        Assertions.assertThat(response.size()).isGreaterThanOrEqualTo(1);
    }

    @Ignore
    @Test
    public void can_retrieve_Pending_and_Active_organisations() {

        Map<String, Object> orgResponseOne =  professionalApiClient.createOrganisation();
        String orgIdentifierOne = (String) orgResponseOne.get("organisationIdentifier");
        assertThat(orgIdentifierOne).isNotEmpty();
        Map<String, Object> orgResponseTwo =  professionalApiClient.createOrganisation();
        String orgIdentifierTwo = (String) orgResponseTwo.get("organisationIdentifier");
        assertThat(orgIdentifierTwo).isNotEmpty();

        professionalApiClient.updateOrganisation(orgIdentifierTwo, hmctsAdmin);
        Map<String, Object> finalResponse = professionalApiClient.retrieveAllOrganisations(puiCaseManager);

        assertThat(finalResponse.get("organisations")).isNotNull();
        Assertions.assertThat(finalResponse.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void can_retrieve_an_organisation_by_request_param_status_equal_to_pending() {

        Map<String, Object> response = professionalApiClient
                .retrieveOrganisationDetailsByStatus(OrganisationStatus.PENDING.name(), puiCaseManager);
        assertThat(response.size()).isGreaterThanOrEqualTo(1);
    }

    @Ignore
    @Test
    public void can_retrieve_an_organisation_by_request_param_status_equal_to_active() {

        Map<String, Object> response = professionalApiClient
                .retrieveOrganisationDetailsByStatus(OrganisationStatus.ACTIVE.name(), puiCaseManager);
        assertThat(response.get("organisations")).isNotNull();
        assertThat(response.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void can_retrieve_400_error_code_by_request_param_status_value_other_than_required_values() {

        professionalApiClient
                .retrieveOrganisationDetailsByUnknownStatus("ACTIV", puiCaseManager);
    }

}
