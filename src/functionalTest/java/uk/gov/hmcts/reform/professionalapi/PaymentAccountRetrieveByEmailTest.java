package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ActiveProfiles("functional")
public class PaymentAccountRetrieveByEmailTest extends FunctionalTestSuite {

    @Test
    public void can_retrieve_payment_accounts_by_email() {

        Map<String, Object> response = professionalApiClient.retrieveOrganisationDetails();
        String responseAsString = response.toString();
        String emailResponseSubString = responseAsString.substring((responseAsString.lastIndexOf("email=") + 6), responseAsString.lastIndexOf(".com") + 4);
        Map<String, Object> emailResponse = professionalApiClient.retrievePaymentAccountsByEmail(emailResponseSubString);
        assertThat(emailResponse).isNotEmpty();
    }

}
