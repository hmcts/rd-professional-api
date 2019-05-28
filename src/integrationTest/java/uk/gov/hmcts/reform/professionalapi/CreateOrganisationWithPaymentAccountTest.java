package uk.gov.hmcts.reform.professionalapi;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.PbaAccountCreationRequest.aPbaPaymentAccount;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;

public class CreateOrganisationWithPaymentAccountTest extends Service2ServiceEnabledIntegrationTest {

    @Test
    public void persists_and_returns_a_single_pba_account_number_for_an_organisation() {
        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("pba123");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccounts(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1").build()))
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        List<PaymentAccount> persistedPaymentAccounts = paymentAccountRepository.findAll();
        assertThat(persistedPaymentAccounts.size()).isEqualTo(1);
        assertThat(persistedPaymentAccounts.get(0).getOrganisation().getName())
                .isEqualTo("some-org-name");
        List<UserAccountMap> userAccountMaps = userAccountMapRepository.findAll();
        assertThat(persistedPaymentAccounts.size()).isEqualTo(userAccountMaps.size());
    }

    @Test
    public void persists_and_returns_multiple_pba_account_numbers_for_an_organisation() {
        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("pbaNumber-1");
        paymentAccounts.add("pbaNumber-2");
        paymentAccounts.add("pbaNumber-3");


        OrganisationCreationRequest organisationCreationRequest =
                someMinimalOrganisationRequest().paymentAccounts(paymentAccounts).build();

        Map<String, Object> createOrganisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        List<PaymentAccount> persistedPaymentAccounts = paymentAccountRepository.findAll();

        assertThat(createOrganisationResponse.get("http_status")).asString().contains("201");

        assertThat(persistedPaymentAccounts).extracting(acc -> acc.getPbaNumber())
                .containsExactlyInAnyOrder(
                        "pbaNumber-1",
                        "pbaNumber-2",
                        "pbaNumber-3");

        assertThat(persistedPaymentAccounts)
                .extracting(paymentAccount -> paymentAccount.getOrganisation().getName())
                .containsExactly(
                        organisationCreationRequest.getName(),
                        organisationCreationRequest.getName(),
                        organisationCreationRequest.getName());


    }

    @Test
    public void still_persists_organisation_when_payment_accounts_list_is_empty() {
        List<String> paymentAccounts = new ArrayList<>();

        OrganisationCreationRequest organisationCreationRequest =
                someMinimalOrganisationRequest()
                        .paymentAccounts(paymentAccounts)
                        .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1").build()))
                        .build();

        Map<String, Object> createOrganisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        List<PaymentAccount> persistedPaymentAccounts = paymentAccountRepository.findAll();

        String orgIdentifierResponse = (String) createOrganisationResponse.get("organisationIdentifier");

        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(UUID.fromString(orgIdentifierResponse));

        assertThat(createOrganisationResponse.get("http_status")).asString().contains("201");

        assertThat(persistedPaymentAccounts).isEmpty();

        assertThat(persistedOrganisation.getName())
                .isEqualTo(organisationCreationRequest.getName());

        List<UserAccountMap> userAccountMaps = userAccountMapRepository.findAll();
        assertThat(userAccountMaps).isEmpty();
        assertThat(persistedPaymentAccounts.size()).isEqualTo(userAccountMaps.size());

    }

    @Test
    public void still_persists_organisation_when_payment_accounts_list_is_null() {

        OrganisationCreationRequest organisationCreationRequest =
                someMinimalOrganisationRequest()
                        .paymentAccounts(null)
                        .build();

        Map<String, Object> createOrganisationResponse = professionalReferenceDataClient
                .createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) createOrganisationResponse.get("organisationIdentifier");

        List<PaymentAccount> persistedPaymentAccounts = paymentAccountRepository.findAll();

        Organisation persistedOrganisation = organisationRepository
                .findByOrganisationIdentifier(UUID.fromString(orgIdentifierResponse));


        assertThat(createOrganisationResponse.get("http_status")).asString().contains("201");

        assertThat(persistedPaymentAccounts).isEmpty();

        assertThat(persistedOrganisation.getName())
                .isEqualTo(organisationCreationRequest.getName());
    }

    @Test
    public void returns_400_when_a_null_pba_number_is_received() {
        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(null);

        OrganisationCreationRequest organisationCreationRequest =
                someMinimalOrganisationRequest()
                        .paymentAccounts(paymentAccounts)
                        .build();

        Map<String, Object> createOrganisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);


        assertThat(createOrganisationResponse.get("http_status")).isEqualTo("400");

        assertThat(paymentAccountRepository.findAll()).isEmpty();

        assertThat(organisationRepository.findAll()).isEmpty();

        assertThat(userAccountMapRepository.findAll()).isEmpty();
    }

    @Test
    public void persists_and_returns_a_multiple_pba_accounts_number_for_multiple_organisations() {
        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("pba123");

        List<String> paymentAccounts2 = new ArrayList<>();
        paymentAccounts.add("pba456");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccounts(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1").build()))
                .build();

        OrganisationCreationRequest organisationCreationRequest2 = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccounts(paymentAccounts2)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone1@somewhere.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1").build()))
                .build();

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse1 = (String) response.get("organisationIdentifier");

        Map<String, Object> response2 =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest2);

        String orgIdentifierResponse2 = (String) response2.get("organisationIdentifier");

        assertThat(orgIdentifierResponse1).isNotEqualTo(orgIdentifierResponse2);

        List<PaymentAccount> persistedPaymentAccounts = paymentAccountRepository.findAll();

        assertThat(persistedPaymentAccounts.size()).isEqualTo(2);

        assertThat(persistedPaymentAccounts.get(0).getOrganisation().getName())
                .isEqualTo(persistedPaymentAccounts.get(1).getOrganisation().getName());

        List<UserAccountMap> userAccountMaps = userAccountMapRepository.findAll();

        assertThat(persistedPaymentAccounts.size()).isEqualTo(userAccountMaps.size());
    }
}
