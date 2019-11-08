package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import io.restassured.specification.RequestSpecification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Slf4j
public class RetrievePaymentAccountTest extends AuthorizationFunctionalTest {

    private RequestSpecification bearerTokenForUser;
    private String orgIdentifier;
    private String userId;

    public RequestSpecification generateBearerTokenForUser(String roleUnderTest, String... otherRoles) {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        orgIdentifier = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);

        List<String> userRoles = Arrays.stream(otherRoles).collect(Collectors.toList());
        userRoles.add(roleUnderTest);

        String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";

        bearerTokenForUser = professionalApiClient.getMultipleAuthHeadersExternal(roleUnderTest, firstName, lastName, userEmail);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();
        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, userCreationRequest);

        userId = (String) newUserResponse.get("userIdentifier");

        return bearerTokenForUser;
    }

    @Test
    public void rdcc117_ac1_pui_finance_manager_can_retrieve_a_list_of_pbas_of_a_given_organisation() {
        Map<String, Object> response = professionalApiClient.retrievePbaAccountsForAnOrganisationExternal(HttpStatus.OK, generateBearerTokenForUser(puiFinanceManager));
        assertThat(response.get("paymentAccount")).asList().hasSize(1);
    }

    @Test
    public void rdcc117_ac2_pui_organisation_manager_can_retrieve_a_list_of_pbas_of_a_given_organisation() {
        Map<String, Object> response = professionalApiClient.retrievePbaAccountsForAnOrganisationExternal(HttpStatus.OK, generateBearerTokenForUser(puiOrgManager));
        assertThat(response.get("paymentAccount")).asList().hasSize(1);
    }

    @Test
    public void rdcc117_ac3_user_without_appropriate_permission_cannot_retrieve_a_list_of_pbas_of_a_given_organisation() {
        Map<String, Object> response = professionalApiClient.retrievePbaAccountsForAnOrganisationExternal(HttpStatus.FORBIDDEN, generateBearerTokenForUser(puiCaseManager));
        //assertThat(response.isEmpty()).isTrue();
        log.info("AC3 Response:::::::::::::" + response);
    }

    @Test
    public void rdcc117_ac4_pui_organisation_or_finance_manager_without_active_status_cannot_retrieve_a_list_of_pbas() {
        //GIVEN I am a pui organisation manager or pui finance manager without Active status
        //AND the given organisation is a valid organisation
        NewUserCreationRequest userCreationRequest = professionalApiClient.createNewUserRequest(puiOrgManager, puiFinanceManager);
        bearerTokenForUser = professionalApiClient.getMultipleAuthHeadersExternal(userCreationRequest.getRoles().get(0), userCreationRequest.getFirstName(), userCreationRequest.getLastName(), userCreationRequest.getEmail());


        Map<String, Object> createOrgResponse = professionalApiClient.createOrganisation();
        orgIdentifier = (String) createOrgResponse.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);

        Map<String, Object> newUserResponse = professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, userCreationRequest);
        userId = (String) newUserResponse.get("userIdentifier");
        log.info("Org with new user::::::::" + newUserResponse);

        UserProfileUpdatedData data = new UserProfileUpdatedData();
        data.setIdamStatus(IdamStatus.SUSPENDED.name());

        Map<String,Object> modifiedStatusResponse = professionalApiClient.modifyUserToExistingUserForPrdAdmin(HttpStatus.OK, data, orgIdentifier, userId);
        //TODO remove logger
        log.info("RESPONSE FROM MODIFY::::::::::::::" + modifiedStatusResponse);
        //WHEN I request the list of PBAs of the given organisation
        Map<String, Object> response = professionalApiClient.retrievePbaAccountsForAnOrganisationExternal(HttpStatus.FORBIDDEN, bearerTokenForUser);
        log.info("RESPONSE FROM RETRIEVE PBAS:::::::::::::" + response);
        //THEN I should not see the list of PBAs of that organisation
        assertThat(response.get("errorMessage")).isNotNull();
        assertThat(response.get("errorMessage")).isEqualTo("9 : Access Denied");
    }

    @Test
    public void can_retrieve_active_organisation_payment_accounts_user_by_email() {
        String email = randomAlphabetic(10) + "@pbasearch.test".toLowerCase();

        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("PBA" + randomAlphabetic(7));

        OrganisationCreationRequest request = someMinimalOrganisationRequest()
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email(email)
                        .jurisdictions(OrganisationFixtures.createJurisdictions())
                        .build())
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
        String email = randomAlphabetic(10) + "@pbasearch.test".toLowerCase();

        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("PBA" + randomAlphabetic(7));

        Map<String, Object> response =  professionalApiClient.createOrganisation(
                someMinimalOrganisationRequest()
                        .paymentAccount(paymentAccounts)
                        .superUser(aUserCreationRequest()
                                .firstName("some-fname")
                                .lastName("some-lname")
                                .email(email)
                                .jurisdictions(OrganisationFixtures.createJurisdictions())
                                .build())
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

    /*
            String userEmail = randomAlphabetic(5).toLowerCase() + "@hotmail.com";
        String lastName = "someLastName";
        String firstName = "someName";
        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-finance-manager");
        userRoles.add("pui-organisation-manager");

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(userEmail)
                .roles(userRoles)
                .jurisdictions(OrganisationFixtures.createJurisdictions())
                .build();

        UserProfileUpdatedData data = new UserProfileUpdatedData();

        data.setFirstName("UpdatedFirstName");
        data.setLastName("UpdatedLastName");
        data.setIdamStatus(IdamStatus.SUSPENDED.name());
     */
}
