package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures;

@RunWith(SpringIntegrationSerenityRunner.class)
@Slf4j
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

    public RequestSpecification generateBearerTokenForPuiManager() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        bearerTokenForPuiCaseManager = professionalApiClient.getMultipleAuthHeadersExternal(puiCaseManager, firstName, lastName, userEmail);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();
        professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest, HttpStatus.CREATED);

        return bearerTokenForPuiCaseManager;
    }

    //@Test
    public void can_retrieve_an_organisation() {
        Map<String, Object> response = professionalApiClient.retrievePbaAccountsForAnOrganisationExternal(HttpStatus.OK, generateBearerTokenForPuiManager());
        assertThat(response).isNotEmpty();
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
        Map<String, Object> finalResponse = professionalApiClient.retrieveAllOrganisations(hmctsAdmin);

        assertThat(finalResponse.get("organisations")).isNotNull();
        Assertions.assertThat(finalResponse.size()).isGreaterThanOrEqualTo(1);
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

}
