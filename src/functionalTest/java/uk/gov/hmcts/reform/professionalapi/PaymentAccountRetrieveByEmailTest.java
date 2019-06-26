package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;

import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
@Ignore
public class PaymentAccountRetrieveByEmailTest extends AuthorizationFunctionalTest {


    @Test
    public void can_retrieve_active_organisation_payment_accounts_user_by_email() {
        String email = randomAlphabetic(10) + "@pbasearch.test";

        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("PBA" + randomAlphabetic(7));

        Map<String, Object> response =  professionalApiClient.createOrganisation(
                someMinimalOrganisationRequest()
                        .paymentAccount(paymentAccounts)
                        .superUser(aUserCreationRequest()
                                .firstName("some-fname")
                                .lastName("some-lname")
                                .email(email)
                                .build())
                        .build());
        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();
        professionalApiClient.updateOrganisation(orgIdentifierResponse, puiCaseManager);
        Map<String, Object> orgResponse = professionalApiClient.retrievePaymentAccountsByEmail(email, puiCaseManager);
        assertThat(orgResponse).isNotEmpty();
        responseValidate(orgResponse);
    }

    @Test
    public void can_return_404_when_pending_organisation_payment_account_user_by_email() {
        String email = randomAlphabetic(10) + "@pbasearch.test";

        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("PBA" + randomAlphabetic(7));

        Map<String, Object> response =  professionalApiClient.createOrganisation(
                someMinimalOrganisationRequest()
                        .paymentAccount(paymentAccounts)
                        .superUser(aUserCreationRequest()
                                .firstName("some-fname")
                                .lastName("some-lname")
                                .email(email)
                                .build())
                        .build());

        professionalApiClient.retrieveBadRequestForPendingOrganisationWithPbaEmail(email, puiCaseManager);
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


}
