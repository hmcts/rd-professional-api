package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createJurisdictions;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;

import org.junit.runner.RunWith;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;


@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
public class LegacyPaymentAccountsByEmailTest extends AuthorizationEnabledIntegrationTest {

    @Test
    public void get_request_returns_correct_payment_accounts_for_user_email_address_ac1() {


        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("PBA1234567");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-")
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("some@email.com")
                        .jurisdictions(createJurisdictions())
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1").build()))
                .build();

        professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .findLegacyPbaAccountsByUserEmail("some@email.com");

        assertThat(orgResponse.get("payment_accounts").toString().equals("PBA1234567"));
        assertThat(orgResponse.get("http_status").toString().contains("OK"));
    }

    @Test
    public void get_request_returns_multiple_payment_accounts_associated_with_user_email_address_ac2() {
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add("pbaNumber-1");
        paymentAccounts.add("pbaNumber-2");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-")
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("some@email.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1").build()))
                .build();

        professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .findLegacyPbaAccountsByUserEmail("some@email.com");

        orgResponse.forEach((k,v) -> {

            if ("payment_accounts".equals(k) && "http_status".equals(k)) {

                assertThat(v.toString().contains("pbaNumber-1, pbaNumber-2"));
                assertThat(v.toString().contains("Ok"));

            }
        });

    }

    @Test
    public void get_request_returns_empty_when_no_payment_accounts_associated_with_user_email_address_ac4() {
        OrganisationCreationRequest organisationCreationRequest = someMinimalOrganisationRequest().build();
        professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        Map<String, Object> orgResponse = professionalReferenceDataClient
                .findLegacyPbaAccountsByUserEmail(organisationCreationRequest.getSuperUser().getEmail());
        assertThat(orgResponse.get("payment_accounts")).asList().isEmpty();
        assertThat(orgResponse.get("http_status").toString().contains("OK"));

    }

    @Test
    public void returns_404_when_email_not_found_ac3() {

        professionalReferenceDataClient.createOrganisation(someMinimalOrganisationRequest().build());

        Map<String, Object> response =
                professionalReferenceDataClient.findLegacyPbaAccountsByUserEmail("wrong@email.com");

        assertThat(response.get("http_status")).isEqualTo("404");
    }

}
