package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.client.ProfessionalApiClient.createOrganisationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

import io.restassured.specification.RequestSpecification;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@ActiveProfiles("functional")
@Slf4j
public class RetrieveOrganisationByOrgIdTest extends AuthorizationFunctionalTest {

    private RequestSpecification bearerTokenForUser;
    private String orgIdentifier;
    private String email = randomAlphabetic(10) + "@pbasearch.test".toLowerCase();
    private String lastName = "someLastName";
    private String firstName = "someName";

    public RequestSpecification generateBearerTokenForUser(String roleUnderTest, String... otherRoles) {
        Map<String, Object> response = professionalApiClient.createOrganisation();
        orgIdentifier = (String) response.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);

        List<String> userRoles = Arrays.stream(otherRoles).collect(Collectors.toList());
        userRoles.add(roleUnderTest);

        bearerTokenForUser = professionalApiClient.getMultipleAuthHeadersExternal(roleUnderTest, firstName, lastName,
                email);

        NewUserCreationRequest userCreationRequest = aNewUserCreationRequest()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .roles(userRoles)
                .build();

        professionalApiClient.addNewUserToAnOrganisation(orgIdentifier, hmctsAdmin, userCreationRequest,
                HttpStatus.CREATED);

        return bearerTokenForUser;
    }

    @Test
    public void rdcc117_ac1_pui_finance_manager_can_retrieve_organisation_by_orgIdentifier_for_external() {
        puiFinanceManagerBearerToken = generateBearerToken(puiFinanceManagerBearerToken, puiFinanceManager);

        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(HttpStatus.OK,
                professionalApiClient.getMultipleAuthHeaders(puiFinanceManagerBearerToken));
        assertThat(response.get("paymentAccount")).asList().hasSize(3);
        responseValidate(response);
    }

    @Test
    public void rdcc117_ac2_pui_organisation_manager_can_retrieve_organisation_by_orgIdentifier_for_external() {
        puiOrgManagerBearerToken = generateBearerToken(puiOrgManagerBearerToken, puiOrgManager);

        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(HttpStatus.OK,
                professionalApiClient.getMultipleAuthHeaders(puiOrgManagerBearerToken));
        assertThat(response.get("paymentAccount")).asList().hasSize(3);
    }


    @Test
    public void rdcc117_ac4_puiOrganisationOrFinanceManagerWithoutActiveStatusCannotRetrieveOrganisationbyOrgId() {

        OrganisationCreationRequest organisationCreationRequest = createOrganisationRequest().superUser(
                createUserForTest()).build();
        Map<String, Object> createOrgResponse = professionalApiClient.createOrganisation(organisationCreationRequest);
        orgIdentifier = (String) createOrgResponse.get("organisationIdentifier");
        professionalApiClient.updateOrganisation(orgIdentifier, hmctsAdmin);

        Map<String, Object> searchUsersResponse = professionalApiClient.searchUsersByOrganisation(orgIdentifier,
                hmctsAdmin, "true", HttpStatus.OK, "");
        bearerTokenForUser = professionalApiClient.getMultipleAuthHeadersExternal(puiOrgManager,
                firstName, lastName, email);
        assertThat(searchUsersResponse.containsValue("PENDING"));

        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(HttpStatus.FORBIDDEN,
                bearerTokenForUser);

        assertThat(response.get("errorMessage")).isNotNull();
        assertThat(response.get("errorMessage")).isEqualTo("9 : Access Denied");
    }

    @Test
    public void can_retrieve_organisation_by_orgIdentifier_for_pui_user_manager_External() {
        puiUserManagerBearerToken = generateBearerToken(puiUserManagerBearerToken, puiUserManager);

        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(HttpStatus.OK,
                professionalApiClient.getMultipleAuthHeaders(puiUserManagerBearerToken));
        assertThat(response).isNotEmpty();
        responseValidate(response);
    }

    @Test
    public void retrieve_an_organisation_with_case_manager_rights_return_200() {
        puiCaseManagerBearerToken = generateBearerToken(puiCaseManagerBearerToken, puiCaseManager);

        Map<String, Object> response = professionalApiClient.retrieveOrganisationByOrgIdExternal(HttpStatus.OK,
                professionalApiClient.getMultipleAuthHeaders(puiCaseManagerBearerToken));
        assertThat(response).isNotEmpty();
        responseValidate(response);
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
                .build();
        return user;
    }

}
