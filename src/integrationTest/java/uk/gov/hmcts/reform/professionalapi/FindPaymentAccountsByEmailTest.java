package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.organisationRequestWithAllFieldsAreUpdated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.professionalapi.configuration.ApplicationConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.service.impl.PaymentAccountServiceImpl;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

public class FindPaymentAccountsByEmailTest extends AuthorizationEnabledIntegrationTest {

    @Autowired
    PaymentAccountServiceImpl paymentAccountService;

    @Autowired
    ApplicationConfiguration configuration;

    @Test
    public void get_request_returns_correct_payment_accounts_associated_with_email() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("PBA1234567");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("some@email.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1").build()))
                .build();

        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String uuid = (String)organisationResponse.get("organisationIdentifier");

        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
                .status(OrganisationStatus.ACTIVE).build();

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, hmctsAdmin, uuid);

        Map<String, Object> orgResponse = professionalReferenceDataClient.findPaymentAccountsByEmail("some@email.com", puiFinanceManager);

        assertThat(orgResponse).isNotEmpty();
        responseValidate(orgResponse);

    }

    @Test
    public void returns_multiple_correct_payment_accounts_associated_with_email() {

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("PBA1234567");
        paymentAccounts.add("PBA1234568");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("some@email.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1").build()))
                .build();

        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String uuid = (String)organisationResponse.get("organisationIdentifier");

        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
                .status(OrganisationStatus.ACTIVE).build();

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, hmctsAdmin, uuid);

        Map<String, Object> orgResponse = professionalReferenceDataClient.findPaymentAccountsByEmail("some@email.com", puiFinanceManager);

        assertThat(orgResponse).isNotEmpty();

        responseValidate(orgResponse);
    }

    @Test
    public void returns_404_organisation_user_accounts_associated_with_email_and_no_payment_account() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("some@email.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1").build()))
                .build();

        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String uuid = (String)organisationResponse.get("organisationIdentifier");

        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
                .status(OrganisationStatus.ACTIVE).build();

        userProfileCreateUserWireMock(HttpStatus.CREATED);
        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, hmctsAdmin, uuid);

        Map<String, Object> orgResponse = professionalReferenceDataClient.findPaymentAccountsByEmail("some@email.com", puiFinanceManager);
        assertThat(orgResponse.get("http_status")).isEqualTo("404");

    }

    @Test
    public void return_404_when_organisation_status_pending_for_pba_user_email_address() {

        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("PBA1234567");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-")
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("some@email.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1").build()))
                .build();

        professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        Map<String, Object> response = professionalReferenceDataClient.findPaymentAccountsByEmail("some@email.com", puiFinanceManager);

        assertThat(response.get("http_status")).isEqualTo("404");

    }

    @Test
    public void returns_404_when_email_not_found() {

        Map<String, Object> response =
                professionalReferenceDataClient.findPaymentAccountsByEmail("wrong@email.com", puiFinanceManager);

        assertThat(response.get("http_status")).isEqualTo("404");
    }

    private void responseValidate(Map<String, Object> orgResponse) {

        assertThat(orgResponse.get("http_status").toString().contains("200"));
        Map<String, Object> activeOrganisation = (Map<String, Object>) orgResponse.get("organisationEntityResponse");

        Map<String, Object> superUser = ((Map<String, Object>) activeOrganisation.get("superUser"));
        assertThat(superUser.get("firstName")).isEqualTo("prashanth");
        assertThat(superUser.get("lastName")).isEqualTo("rao");
        assertThat(superUser.get("email")).isEqualTo("super.user@hmcts.net");


    }
}
