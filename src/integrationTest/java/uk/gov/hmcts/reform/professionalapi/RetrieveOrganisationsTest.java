package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest.aNewUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.createJurisdictions;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.organisationRequestWithAllFields;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.junit.Test;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

@Slf4j
public class RetrieveOrganisationsTest extends AuthorizationEnabledIntegrationTest {

    @SuppressWarnings("unchecked")
    @Test
    public void persists_and_returns_organisation_details() {

        String orgIdentifierResponse = createOrganisationRequest("PENDING");
        assertThat(orgIdentifierResponse).isNotEmpty();
        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveSingleOrganisation(orgIdentifierResponse, hmctsAdmin);

        assertThat(orgResponse.get("http_status").toString().contains("OK"));
        assertThat(orgResponse.get("organisationIdentifier")).isEqualTo(orgIdentifierResponse);

        assertThat(orgResponse.get("name")).isEqualTo("some-org-name");
        assertThat(orgResponse.get("sraId")).isEqualTo("sra-id");
        assertThat(orgResponse.get("sraRegulated")).isEqualTo(false);
        assertThat(orgResponse.get("companyUrl")).isEqualTo("company-url");
        assertThat(orgResponse.get("companyNumber")).isNotNull();

        Map<String, Object> superUser = ((Map<String, Object>) orgResponse.get("superUser"));
        assertThat(superUser.get("firstName")).isEqualTo("some-fname");
        assertThat(superUser.get("lastName")).isEqualTo("some-lname");
        assertThat(superUser.get("email")).isEqualTo("someone@somewhere.com");

        List<String> accounts = ((List<String>)  orgResponse.get("paymentAccount"));
        assertThat(accounts.get(0).equals("pba123"));

        Map<String, Object> contactInfo = ((List<Map<String, Object>>) orgResponse.get("contactInformation")).get(0);
        assertThat(contactInfo.get("addressLine1")).isEqualTo("addressLine1");
        assertThat(contactInfo.get("addressLine2")).isEqualTo("addressLine2");
        assertThat(contactInfo.get("addressLine3")).isEqualTo("addressLine3");
        assertThat(contactInfo.get("county")).isEqualTo("county");
        assertThat(contactInfo.get("country")).isEqualTo("country");
        assertThat(contactInfo.get("townCity")).isEqualTo("town-city");
        assertThat(contactInfo.get("postCode")).isEqualTo("some-post-code");

        Map<String, Object> dxAddress = ((List<Map<String, Object>>) contactInfo.get("dxAddress")).get(0);
        assertThat(dxAddress.get("dxNumber")).isEqualTo("DX 1234567890");
        assertThat(dxAddress.get("dxExchange")).isEqualTo("dxExchange");

        log.info("RetrieveOrganisationsTest:Received response to retrieve an organisation details...");
    }

    @Test
    public void persists_and_returns_all_organisations() {
        Map<String, Object> orgResponse1 = professionalReferenceDataClient.createOrganisation(someMinimalOrganisationRequest()
                .build());
        Map<String, Object> orgResponse2 = professionalReferenceDataClient.createOrganisation(someMinimalOrganisationRequest()
                .name("some-other-org-name")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someoneElse@somewhere.com")
                        .jurisdictions(createJurisdictions())
                        .build())
                .build());

        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisations(hmctsAdmin);
        assertThat(orgResponse.get("http_status").toString().contains("OK"));
        assertThat(((List<?>) orgResponse.get("organisations")).size()).isEqualTo(2);
    }

    @Test
    public void error_if_organisation_id_invalid() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveSingleOrganisation("They're taking the hobbits to Isengard!", hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("404");
    }

    @Test
    public void error_if_organisation_id_not_found() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveSingleOrganisation("11AA116", hmctsAdmin);
        assertThat(response.get("http_status")).isEqualTo("404");
    }

    @Test
    public void forbidden_if_pui_case_manager_user_try_access_organisation_id_without_role_access() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveExternalOrganisation("11AA116", puiCaseManager);
        assertThat(response.get("http_status")).isEqualTo("403");
    }

    @Test
    public void forbidden_if_pui_user_manager_try_access_organisation_id_without_role_access() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveExternalOrganisation("11AA116", puiUserManager);
        assertThat(response.get("http_status")).isEqualTo("403");
    }

    @Test
    public void forbidden_if_pui_finance_manager_try_access_organisation_id_without_role_access() {
        Map<String, Object> response = professionalReferenceDataClient.retrieveExternalOrganisation("11AA116", puiFinanceManager);
        assertThat(response.get("http_status")).isEqualTo("403");
    }



    @Test
    public void persists_and_returns_all_organisations_details_by_pending_status() {

        String organisationIdentifier = createOrganisationRequest("PENDING");
        assertThat(organisationIdentifier).isNotEmpty();
        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisationDetailsByStatusTest(OrganisationStatus.PENDING.name(), hmctsAdmin);
        assertThat(orgResponse.get("organisations")).isNotNull();
        assertThat(orgResponse.get("organisations")).asList().isNotEmpty();
        assertThat(orgResponse.get("http_status").toString().contains("OK"));
    }

    @Test
    public void persists_and_returns_all_organisations_details_by_active_status() {

        Map<String, Object> orgResponse;
        String organisationIdentifier = createOrganisationRequest("ACTIVE");
        assertThat(organisationIdentifier).isNotEmpty();
        orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisationDetailsByStatusTest(OrganisationStatus.ACTIVE.name(), hmctsAdmin);
        assertThat(orgResponse.get("http_status").toString().contains("OK"));

        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
                .status("ACTIVE").build();
        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest,hmctsAdmin, organisationIdentifier);


        assertThat(responseForOrganisationUpdate.get("http_status")).isEqualTo(200);

        orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisationDetailsByStatusTest(OrganisationStatus.ACTIVE.name(), hmctsAdmin);

        assertThat(orgResponse.get("organisations")).asList().isNotEmpty();
        assertThat(orgResponse.get("http_status").toString().contains("OK"));

        Map<String, Object> activeOrganisation = ((List<Map<String, Object>>) orgResponse.get("organisations")).get(0);

        Map<String, Object> superUser = ((Map<String, Object>) activeOrganisation.get("superUser"));
        assertThat(superUser.get("firstName")).isEqualTo("prashanth");
        assertThat(superUser.get("lastName")).isEqualTo("rao");
        assertThat(superUser.get("email")).isEqualTo("super.user@hmcts.net");
    }

    @Test
    public void persists_and_return_empty_organisation_details_when_no_status_found_in_the_db() {

        String organisationIdentifier = createOrganisationRequest("ACTIVE");
        assertThat(organisationIdentifier).isNotEmpty();
        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisationDetailsByStatusTest(OrganisationStatus.ACTIVE.name(), puiCaseManager);
        assertThat(orgResponse.get("http_status").toString().contains("OK"));
    }

    @Test
    public void return_404_when_invalid_status_send_in_the_request_param() {

        String organisationIdentifier = createOrganisationRequest("ACTIVE");
        assertThat(organisationIdentifier).isNotEmpty();
        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisationDetailsByStatusTest("ACTIV", hmctsAdmin);
        assertThat(orgResponse.get("http_status").toString().contains("404"));
    }

    private String createOrganisationRequest(String status) {
        OrganisationCreationRequest organisationCreationRequest = null;
        organisationCreationRequest = organisationRequestWithAllFields().status(status).build();
        Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        return (String) responseForOrganisationCreation.get("organisationIdentifier");
    }

    @Test
    public void retrieve_organisation_should_have_single_super_user() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);

        List<String> user1Roles = new ArrayList<>();
        user1Roles.add("pui-user-manager");

        List<String> user2Roles = new ArrayList<>();
        user2Roles.add("pui-user-manager");
        user2Roles.add("organisation-admin");

        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();

        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) organisationResponse.get("organisationIdentifier");

        professionalReferenceDataClient.updateOrganisation(someMinimalOrganisationRequest().status("ACTIVE").build(), hmctsAdmin, orgIdentifierResponse);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        NewUserCreationRequest userCreationRequest1 = aNewUserCreationRequest()
                .firstName("someName1")
                .lastName("someLastName1")
                .email("some@email.com")
                .roles(user1Roles)
                .jurisdictions(createJurisdictions())
                .build();
        Map<String, Object> newUserResponse1 =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest1, hmctsAdmin);

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        NewUserCreationRequest userCreationRequest2 = aNewUserCreationRequest()
                .firstName("someName2")
                .lastName("someLastName2")
                .email("some@email2.com")
                .roles(user2Roles)
                .jurisdictions(createJurisdictions())
                .build();
        Map<String, Object> newUserResponse2 =
                professionalReferenceDataClient.addUserToOrganisation(orgIdentifierResponse, userCreationRequest2, hmctsAdmin);

        Organisation persistedOrganisation = organisationRepository.findByOrganisationIdentifier(orgIdentifierResponse);

        assertThat(persistedOrganisation.getUsers().size()).isEqualTo(3);

        SuperUser persistedSuperUser = persistedOrganisation.getUsers().get(0);

        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveSingleOrganisation(orgIdentifierResponse, hmctsAdmin);

        assertThat(orgResponse.get("http_status").toString().contains("OK"));
        assertThat(orgResponse.get("organisationIdentifier")).isEqualTo(orgIdentifierResponse);

        Map<String, Object> superUser = ((Map<String, Object>) orgResponse.get("superUser"));
        assertThat(superUser.get("firstName")).isEqualTo("prashanth");
        assertThat(superUser.get("lastName")).isEqualTo("rao");
        assertThat(superUser.get("email")).isEqualTo("super.user@hmcts.net");

    }

    @Test
    public void  persists_and_return_forbidden_when_no_role_associated_with_end_point() {

        String orgIdentifierResponse = createOrganisationRequest("PENDING");
        assertThat(orgIdentifierResponse).isNotEmpty();
        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveSingleOrganisation(orgIdentifierResponse, hmctsAdmin);

        assertThat(orgResponse.get("http_status").toString().contains("403"));

    }

    @Test
    public void  persists_and_return_pending_from_prd_and_active_org_details_from_up_and_combine_both() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        OrganisationCreationRequest organisationRequest = anOrganisationCreationRequest()
                .name("org-name")
                .superUser(aUserCreationRequest()
                        .firstName("fname")
                        .lastName("lname1")
                        .email("someone11@somewhere.com")
                        .jurisdictions(createJurisdictions())
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine2").build())).build();

        Map<String, Object> responseForOrganisationCreation = professionalReferenceDataClient.createOrganisation(organisationRequest);

        OrganisationCreationRequest organisationCreationRequest = organisationRequestWithAllFields().build();

        Map<String, Object> response = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        String orgId = (String) response.get("organisationIdentifier");

        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated().status("ACTIVE").build();

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, hmctsAdmin, orgId);

        assertThat(responseForOrganisationUpdate.get("http_status")).isEqualTo(200);

        Map<String, Object> orgResponse =  professionalReferenceDataClient.retrieveAllOrganisations(hmctsAdmin);

        assertThat(orgResponse.get("http_status").toString().contains("200"));
        assertThat(orgResponse.get("organisations")).asList().isNotEmpty();
        assertThat(orgResponse.get("organisations")).asList().size().isEqualTo(2);

        Map<String, Object> organisation = ((List<Map<String, Object>>) orgResponse.get("organisations")).get(0);

        Map<String, Object> superUser = ((Map<String, Object>) organisation.get("superUser"));

        assertThat(superUser.get("firstName")).isEqualTo("fname");
        assertThat(superUser.get("lastName")).isEqualTo("lname1");
        assertThat(superUser.get("email")).isEqualTo("someone11@somewhere.com");

        Map<String, Object> organisationSecond = ((List<Map<String, Object>>) orgResponse.get("organisations")).get(1);

        Map<String, Object> superUserSecond = ((Map<String, Object>) organisationSecond.get("superUser"));

        assertThat(superUserSecond.get("firstName")).isEqualTo("prashanth");
        assertThat(superUserSecond.get("lastName")).isEqualTo("rao");
        assertThat(superUserSecond.get("email")).isEqualTo("super.user@hmcts.net");
    }

}
