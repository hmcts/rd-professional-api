package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
public class PaymentAccountRetrieveByEmailTest extends FunctionalTestSuite {

    @Test
    public void can_retrieve_payment_accounts_by_email() {
        String email = randomAlphabetic(10) + "@pbasearch.test";

        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(randomAlphabetic(10));

        professionalApiClient.createOrganisation(
                someMinimalOrganisationRequest()
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                           .firstName("some-fname")
                           .lastName("some-lname")
                           .email(email)
                           .build())
                .build());

        Map<String, Object> emailResponse = professionalApiClient.retrievePaymentAccountsByEmail(email);
        assertThat(emailResponse).isNotEmpty();
    }

}
