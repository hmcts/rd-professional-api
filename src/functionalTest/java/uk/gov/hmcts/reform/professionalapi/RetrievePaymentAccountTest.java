package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createJurisdictions;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;


@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class RetrievePaymentAccountTest extends AuthorizationFunctionalTest {

    private RequestSpecification bearerTokenForUser;
    private String orgIdentifier;
    private String email = randomAlphabetic(10) + "@pbasearch.test".toLowerCase();
    private String lastName = "someLastName";
    private String firstName = "someName";

    public RequestSpecification generateBearerTokenForUser(String roleUnderTest, String... otherRoles) {
        List<String> userRoles = Arrays.stream(otherRoles).collect(Collectors.toList());
        userRoles.add(roleUnderTest);

        // creating user in idam with the same email used to create Organisation so that status is already Active in UP
        bearerToken = professionalApiClient.getMultipleAuthHeadersExternal(roleUnderTest, firstName, lastName, email);

        //create organisation with the same Super User Email
        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().superUser(aUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .jurisdictions(createJurisdictions())
                .build()).build();

        Map<String, Object> response = professionalApiClient.createOrganisation(organisationCreationRequest);
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);

        return bearerTokenForUser;
    }

    @Before
    public void createAndUpdateOrganisation() {
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");
        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        // creating user in idam with the same email used to create Organisation so that status is already Active in UP
        bearerToken = professionalApiClient.getMultipleAuthHeadersExternal(hmctsAdmin, firstName, lastName, userEmail);

        //create organisation with the same Super User Email
        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().superUser(aUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .jurisdictions(createJurisdictions())
                .build()).build();

        Map<String, Object> response = professionalApiClient.createOrganisation(organisationCreationRequest);
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifierResponse, hmctsAdmin);
    }

    @Test
    public void rdcc117_ac1_pui_finance_manager_can_retrieve_a_list_of_pbas_of_a_given_organisation() {
        Map<String, Object> response = professionalApiClient.retrievePbaAccountsForAnOrganisationExternal(HttpStatus.OK, generateBearerTokenForUser(puiFinanceManager));
        assertThat(response.get("paymentAccount")).asList().hasSize(3);
    }

    @Test
    public void rdcc117_ac2_pui_organisation_manager_can_retrieve_a_list_of_pbas_of_a_given_organisation() {
        Map<String, Object> response = professionalApiClient.retrievePbaAccountsForAnOrganisationExternal(HttpStatus.OK, generateBearerTokenForUser(puiOrgManager));
        assertThat(response.get("paymentAccount")).asList().hasSize(3);
    }

    @Test
    public void rdcc117_ac3_user_without_appropriate_permission_cannot_retrieve_a_list_of_pbas_of_a_given_organisation() {
        Map<String, Object> response = professionalApiClient.retrievePbaAccountsForAnOrganisationExternal(HttpStatus.FORBIDDEN, generateBearerTokenForUser(puiUserManager));
        assertThat(response.get("errorMessage")).isNotNull();
        assertThat(response.get("errorMessage")).isEqualTo("9 : Access Denied");
    }

    @Test
    public void rdcc117_ac4_pui_organisation_or_finance_manager_without_active_status_cannot_retrieve_a_list_of_pbas() {

        OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest().superUser(createUserForTest()).build();
        Map<String, Object> createOrgResponse = professionalApiClient.createOrganisation(organisationCreationRequest);
        orgIdentifier = (String) createOrgResponse.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);

        Map<String, Object> searchUsersResponse = professionalApiClient.searchUsersByOrganisation(orgIdentifier, puiOrgManager, "true", HttpStatus.OK);
        bearerTokenForUser = professionalApiClient.getMultipleAuthHeadersExternal(puiOrgManager, firstName, lastName, email);
        assertThat(searchUsersResponse.containsValue("PENDING"));

        Map<String, Object> response = professionalApiClient.retrievePbaAccountsForAnOrganisationExternal(HttpStatus.FORBIDDEN, bearerTokenForUser);

        assertThat(response.get("errorMessage")).isNotNull();
        assertThat(response.get("errorMessage")).isEqualTo("9 : Access Denied");
    }

    @Test
    public void can_retrieve_active_organisation_payment_accounts_user_by_email() {

        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA" + randomAlphabetic(7));

        OrganisationCreationRequest request = someMinimalOrganisationRequest()
                .paymentAccount(paymentAccounts)
                .superUser(createUserForTest())
                .build();

        Map<String, Object> response =  professionalApiClient.createOrganisation(request);
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();
        request.setStatus("ACTIVE");
        professionalApiClient.updateOrganisation(request, hmctsAdmin, orgIdentifierResponse);
        Map<String, Object> orgResponse = professionalApiClient.retrievePaymentAccountsByEmail(email.toLowerCase(), hmctsAdmin);
        assertThat(orgResponse).isNotEmpty();
        responseValidate(orgResponse);
    }

    @Test
    public void can_return_404_when_pending_organisation_payment_account_user_by_email() {

        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA" + randomAlphabetic(7));

        Map<String, Object> response =  professionalApiClient.createOrganisation(
                someMinimalOrganisationRequest()
                        .paymentAccount(paymentAccounts)
                        .superUser(createUserForTest())
                        .build());

        professionalApiClient.retrieveBadRequestForPendingOrganisationWithPbaEmail(email, hmctsAdmin);
    }

    private void responseValidate(Map<String, Object> orgResponse) {

        orgResponse.forEach((k,v) -> {

            if ("organisationIdentifier".equals(k) && "http_status".equals(k)
                    && "name".equals(k) &&  "status".equals(k)
                    && "superUser".equals(k) && "paymentAccount".equals(k)) {

                Assertions.assertThat(v.toString()).isNotEmpty();
                Assertions.assertThat(v.toString().contains("Ok"));
                Assertions.assertThat(v.toString().contains("some-org-name"));
                Assertions.assertThat(v.toString().equals("ACTIVE"));
                Assertions.assertThat(v.toString()).isNotEmpty();
            }

        });

    }

    private UserCreationRequest createUserForTest() {
        UserCreationRequest user = aUserCreationRequest()
                .firstName("some-fname")
                .lastName("some-lname")
                .email(email)
                .jurisdictions(createJurisdictions())
                .build();
        return user;
    }

}
