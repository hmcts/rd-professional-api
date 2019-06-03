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

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.service.impl.PaymentAccountServiceImpl;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;

public class FindPaymentAccountsByEmailTest extends Service2ServiceEnabledIntegrationTest {

    @Autowired
    PaymentAccountServiceImpl paymentAccountService;


    @Test
    public void get_request_returns_correct_payment_accounts_associated_with_email() {

        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("pba123");

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

        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String uuid = (String)organisationResponse.get("organisationIdentifier");

        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
                .status(OrganisationStatus.ACTIVE).build();

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, uuid);

        Map<String, Object> orgResponse = professionalReferenceDataClient.findPaymentAccountsByEmail("some@email.com");

        assertThat(orgResponse).isNotEmpty();

        responseValidate(orgResponse);
    }

    @Test
    public void returns_multiple_correct_payment_accounts_associated_with_email() {

        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("pba123");
        paymentAccounts.add("pba124");

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

        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String uuid = (String)organisationResponse.get("organisationIdentifier");

        OrganisationCreationRequest organisationUpdateRequest = organisationRequestWithAllFieldsAreUpdated()
                .status(OrganisationStatus.ACTIVE).build();

        Map<String, Object> responseForOrganisationUpdate =
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, uuid);

        Map<String, Object> orgResponse = professionalReferenceDataClient.findPaymentAccountsByEmail("some@email.com");

        assertThat(orgResponse).isNotEmpty();

        responseValidate(orgResponse);
    }

    @Test
    public void returns_200_organisation_user_accounts_associated_with_email_and_no_payment_account() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-")
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
                professionalReferenceDataClient.updateOrganisation(organisationUpdateRequest, uuid);

        Map<String, Object> orgResponse = professionalReferenceDataClient.findPaymentAccountsByEmail("some@email.com");

        orgResponse.forEach((k,v) -> {

            if ("organisationIdentifier".equals(k) && "http_status".equals(k)
                    && "name".equals(k) &&  "status".equals(k) && "sraId".equals(k)
                    && "sraRegulated".equals(k) && "companyNumber".equals(k)
                    && "companyUrl".equals(k) &&  "superUser".equals(k)) {

                assertThat(v.toString()).isNotEmpty();
                assertThat(v.toString().contains("Ok"));
                assertThat(v.toString().contains("some-org-name1"));
                assertThat(v.toString().equals("ACTIVE"));
                assertThat(v.toString().equals("sra-id1"));
                assertThat(v.toString().equals("true"));
                assertThat(v.toString().equals("company1"));
                assertThat(v.toString().equals("company-url1"));
            }

        });
    }

    @Test
    public void return_404_when_organisation_status_pending_for_pba_user_email_address() {

        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("pba123");

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

        Map<String, Object> response = professionalReferenceDataClient.findPaymentAccountsByEmail("some@email.com");

        assertThat(response.get("http_status")).isEqualTo("404");

    }

    @Test
    public void returns_404_when_email_not_found() {

        Map<String, Object> response =
                professionalReferenceDataClient.findPaymentAccountsByEmail("wrong@email.com");

        assertThat(response.get("http_status")).isEqualTo("404");
    }

    private void responseValidate(Map<String, Object> orgResponse) {

        orgResponse.forEach((k,v) -> {

            if ("organisationIdentifier".equals(k) && "http_status".equals(k)
                    && "name".equals(k) &&  "status".equals(k)
                    && "sraId".equals(k) && "sraRegulated".equals(k)
                    && "companyNumber".equals(k) && "companyUrl".equals(k)
                    &&  "superUser".equals(k) && "paymentAccount".equals(k)) {

                assertThat(v.toString()).isNotEmpty();
                assertThat(v.toString().contains("Ok"));
                assertThat(v.toString().contains("some-org-name1"));
                assertThat(v.toString().equals("ACTIVE"));
                assertThat(v.toString().equals("sra-id1"));
                assertThat(v.toString().equals("true"));
                assertThat(v.toString().equals("company1"));
                assertThat(v.toString().equals("company-url1"));
                assertThat(v.toString().contains("pba123,pba124"));
            }

        });

    }
}
