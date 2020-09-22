package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@Slf4j
@Ignore
public class OrganisationRetrieveTest extends AuthorizationFunctionalTest {

    @Test
    public void can_retrieve_all_organisations() {
        professionalApiClient.createOrganisation();

        Map<String, Object> response = professionalApiClient.retrieveAllOrganisations(hmctsAdmin);
        assertThat(response.get("organisations")).isNotNull();
        Assertions.assertThat(response.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void can_retrieve_a_single_organisation() {
        Map<String, Object> response = professionalApiClient.createOrganisation();

        response = professionalApiClient.retrieveOrganisationDetails((String) response.get("organisationIdentifier"),
                puiCaseManager,HttpStatus.OK);
        validateSingleOrgResponse(response, "PENDING");

    }

    @Test
    public void retrieve_an_organisation_with_case_manager_rights_return_200() {
        Map<String, Object> response = professionalApiClient.retrievePbaAccountsForAnOrganisationExternal(HttpStatus.OK,
                generateBearerTokenFor(puiCaseManager));
        validateSingleOrgResponse(response, "ACTIVE");
    }

    @Test
    public void retrieve_an_organisation_with_user_manager_rights_return_200() {
        Map<String, Object> response = professionalApiClient.retrievePbaAccountsForAnOrganisationExternal(HttpStatus.OK,
                generateBearerTokenFor(puiUserManager));
        validateSingleOrgResponse(response, "ACTIVE");
    }

    @Test
    public void can_retrieve_Pending_and_Active_organisations() {

        Map<String, Object> orgResponseOne =  professionalApiClient.createOrganisation();
        String orgIdentifierOne = (String) orgResponseOne.get("organisationIdentifier");
        assertThat(orgIdentifierOne).isNotEmpty();
        Map<String, Object> orgResponseTwo =  professionalApiClient.createOrganisation();
        String orgIdentifierTwo = (String) orgResponseTwo.get("organisationIdentifier");
        assertThat(orgIdentifierTwo).isNotEmpty();

        professionalApiClient.updateOrganisation(orgIdentifierTwo, hmctsAdmin);
        Map<String, Object> newOrgResponse = professionalApiClient.retrieveOrganisationDetails(orgIdentifierTwo,
                hmctsAdmin,HttpStatus.OK);
        Map<String, Object> finalResponse = professionalApiClient.retrieveAllOrganisations(hmctsAdmin);

        assertThat(finalResponse.get("organisations")).isNotNull();
        Assertions.assertThat(finalResponse.size()).isGreaterThanOrEqualTo(1);
        assertThat(newOrgResponse.get("paymentAccount")).asList().size().isEqualTo(3);
        assertThat(newOrgResponse.get("contactInformation")).asList().size().isEqualTo(2);

        Map<String, Object> contactInfo1 = ((List<Map<String, Object>>) newOrgResponse.get("contactInformation"))
                .get(0);
        Map<String, Object> contactInfo2 = ((List<Map<String, Object>>) newOrgResponse.get("contactInformation"))
                .get(1);

        assertThat(contactInfo1.get("addressLine1")).isNotNull();
        assertThat(contactInfo2.get("addressLine1")).isNotNull();

        Map<String, Object> dxAddress = ((List<Map<String, Object>>) contactInfo1.get("dxAddress")).get(0);
        Map<String, Object> dxAddress2 = ((List<Map<String, Object>>) contactInfo1.get("dxAddress")).get(1);

        assertThat(dxAddress.get("dxNumber")).isNotNull();
        assertThat(dxAddress.get("dxExchange")).isEqualTo("dxExchange");
        assertThat(dxAddress2.get("dxNumber")).isNotNull();
        assertThat(dxAddress2.get("dxExchange")).isEqualTo("dxExchange");
    }

    @Test
    public void can_retrieve_an_organisation_by_request_param_status_equal_to_pending() {

        Map<String, Object> response = professionalApiClient
                .retrieveOrganisationDetailsByStatus(OrganisationStatus.PENDING.name(), hmctsAdmin);
        assertThat(response.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void can_retrieve_an_organisation_by_request_param_status_equal_to_active() {

        Map<String, Object> response = professionalApiClient
                .retrieveOrganisationDetailsByStatus(OrganisationStatus.ACTIVE.name(), hmctsAdmin);
        assertThat(response.get("organisations")).isNotNull();
        assertThat(response.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void can_retrieve_400_error_code_by_request_param_status_value_other_than_required_values() {

        professionalApiClient
                .retrieveOrganisationDetailsByUnknownStatus("ACTIV", hmctsAdmin);
    }

    public void validateSingleOrgResponse(Map<String, Object> response, String status) {

        Assertions.assertThat(response.size()).isGreaterThanOrEqualTo(1);
        assertThat(response.get("organisationIdentifier")).isNotNull();
        assertThat(response.get("name")).isNotNull();
        assertThat(response.get("status")).isEqualTo(status);
        assertThat(response.get("sraId")).isNotNull();
        assertThat(response.get("sraRegulated")).isNotNull();
        assertThat(response.get("companyNumber")).isNotNull();
        assertThat(response.get("companyUrl")).isNotNull();
        assertThat(response.get("superUser")).isNotNull();
        assertThat(response.get("paymentAccount")).isNotNull();
        assertThat(response.get("contactInformation")).isNotNull();

    }

}
