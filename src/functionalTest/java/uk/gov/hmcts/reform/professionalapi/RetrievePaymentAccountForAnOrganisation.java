package uk.gov.hmcts.reform.professionalapi;

import io.restassured.specification.RequestSpecification;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;

public class RetrievePaymentAccountForAnOrganisation extends AuthorizationFunctionalTest {

    RequestSpecification bearerTokenForPuiFinanceManager;

    public RequestSpecification generateBearerTokenForPuiFinanceManager() {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        bearerTokenForPuiFinanceManager = professionalApiClient.getMultipleAuthHeadersExternal(puiFinanceManager, firstName, lastName, userEmail);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();
        professionalApiClient.addNewUserToAnOrganisation(orgIdentifierResponse, hmctsAdmin, userCreationRequest);

        return bearerTokenForPuiFinanceManager;
    }

    @Test
    public void ac1_HMCTS_user_can_retrieve_a_list_of_PBAs_of_a_given_organisation() {
        Map<String, Object> response = professionalApiClient.retrievePbaAccountsForAnOrganisationExternal(HttpStatus.OK, generateBearerTokenForPuiFinanceManager());
        assertThat(response.get("paymentAccount")).asList().hasSize(1);
    }
}
