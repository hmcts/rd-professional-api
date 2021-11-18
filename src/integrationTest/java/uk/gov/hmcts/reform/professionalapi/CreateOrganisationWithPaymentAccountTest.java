package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.PENDING;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;


class CreateOrganisationWithPaymentAccountTest extends AuthorizationEnabledIntegrationTest {

    @Test
    void persists_and_returns_a_single_pba_account_number_for_an_organisation() {
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .build()))
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get(ORG_IDENTIFIER);
        List<PaymentAccount> persistedPaymentAccounts = paymentAccountRepository.findAll();
        assertThat(persistedPaymentAccounts.size()).isEqualTo(1);
        assertThat(persistedPaymentAccounts.get(0).getOrganisation().getName())
                .isEqualTo("some-org-name");
        List<UserAccountMap> userAccountMaps = userAccountMapRepository.findAll();
        assertThat(persistedPaymentAccounts.size()).isEqualTo(userAccountMaps.size());
        assertThat(persistedPaymentAccounts.get(0).getPbaStatus()).isEqualTo(PENDING);
    }

    @Test
    void persists_and_returns_multiple_pba_account_numbers_for_an_organisation() {
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");
        paymentAccounts.add("PBA2345678");
        paymentAccounts.add("PBA3456789");


        OrganisationCreationRequest organisationCreationRequest =
                someMinimalOrganisationRequest().paymentAccount(paymentAccounts).build();

        Map<String, Object> createOrganisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        List<PaymentAccount> persistedPaymentAccounts = paymentAccountRepository.findAll();

        assertThat(createOrganisationResponse.get("http_status")).asString().contains("201");

        assertThat(persistedPaymentAccounts).extracting(acc -> acc.getPbaNumber())
                .containsExactlyInAnyOrder(
                        "PBA1234567",
                        "PBA2345678",
                        "PBA3456789");

        assertThat(persistedPaymentAccounts)
                .extracting(paymentAccount -> paymentAccount.getOrganisation().getName())
                .containsExactly(
                        organisationCreationRequest.getName(),
                        organisationCreationRequest.getName(),
                        organisationCreationRequest.getName());


    }

    @Test
    void still_persists_organisation_when_payment_accounts_list_is_empty() {
        Set<String> paymentAccounts = new HashSet<>();

        OrganisationCreationRequest organisationCreationRequest =
                someMinimalOrganisationRequest()
                        .paymentAccount(paymentAccounts)
                        .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                                .addressLine1("addressLine1").build()))
                        .build();

        Map<String, Object> createOrganisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        List<PaymentAccount> persistedPaymentAccounts = paymentAccountRepository.findAll();

        String orgIdentifierResponse = (String) createOrganisationResponse.get(ORG_IDENTIFIER);

        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(orgIdentifierResponse);

        assertThat(createOrganisationResponse.get("http_status")).asString().contains("201");

        assertThat(persistedPaymentAccounts).isEmpty();

        assertThat(persistedOrganisation.getName())
                .isEqualTo(organisationCreationRequest.getName());

        List<UserAccountMap> userAccountMaps = userAccountMapRepository.findAll();
        assertThat(userAccountMaps).isEmpty();
        assertThat(persistedPaymentAccounts.size()).isEqualTo(userAccountMaps.size());

    }

    @Test
    void still_persists_organisation_when_payment_accounts_list_is_null() {

        OrganisationCreationRequest organisationCreationRequest =
                someMinimalOrganisationRequest()
                        .paymentAccount(null)
                        .build();

        Map<String, Object> createOrganisationResponse = professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) createOrganisationResponse.get(ORG_IDENTIFIER);

        List<PaymentAccount> persistedPaymentAccounts = paymentAccountRepository.findAll();

        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(orgIdentifierResponse);


        assertThat(createOrganisationResponse.get("http_status")).asString().contains("201");

        assertThat(persistedPaymentAccounts).isEmpty();

        assertThat(persistedOrganisation.getName())
                .isEqualTo(organisationCreationRequest.getName());
    }

    @Test
    void returns_400_when_a_null_pba_number_is_received() {
        cleanupTestData();
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add(null);

        OrganisationCreationRequest organisationCreationRequest =
                someMinimalOrganisationRequest()
                        .paymentAccount(paymentAccounts)
                        .build();

        Map<String, Object> createOrganisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);


        assertThat(createOrganisationResponse.get("http_status")).isEqualTo("400");

        assertThat(paymentAccountRepository.findAll()).isEmpty();

        assertThat(organisationRepository.findAll()).isEmpty();

        assertThat(userAccountMapRepository.findAll()).isEmpty();
    }

    @Test
    void persists_and_returns_a_multiple_pba_accounts_number_for_multiple_organisations() {
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        Set<String> paymentAccounts2 = new HashSet<>();
        paymentAccounts.add("PBA7654321");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .build()))
                .build();

        OrganisationCreationRequest organisationCreationRequest2 = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccount(paymentAccounts2)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone1@somewhere.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .build()))
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse1 = (String) response.get(ORG_IDENTIFIER);

        Map<String, Object> response2 =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest2);

        String orgIdentifierResponse2 = (String) response2.get(ORG_IDENTIFIER);

        assertThat(orgIdentifierResponse1).isNotEqualTo(orgIdentifierResponse2);

        List<PaymentAccount> persistedPaymentAccounts = paymentAccountRepository.findAll();

        assertThat(persistedPaymentAccounts.size()).isEqualTo(2);

        assertThat(persistedPaymentAccounts.get(0).getOrganisation().getName())
                .isEqualTo(persistedPaymentAccounts.get(1).getOrganisation().getName());

        List<UserAccountMap> userAccountMaps = userAccountMapRepository.findAll();

        assertThat(persistedPaymentAccounts.size()).isEqualTo(userAccountMaps.size());
    }
}
