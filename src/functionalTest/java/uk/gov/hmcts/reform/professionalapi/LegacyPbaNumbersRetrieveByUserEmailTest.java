package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;


@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
public class LegacyPbaNumbersRetrieveByUserEmailTest extends AuthorizationFunctionalTest {

    @Test
    public void can_retrieve_payment_numbers_by_user_email() {

        String email = randomAlphabetic(10) + "@pbasearch.test".toLowerCase();

        Set<String> paymentAccounts = new HashSet<>();

        String pbaNumber = "PBA" + randomAlphabetic(7);

        paymentAccounts.add(pbaNumber);

        professionalApiClient.createOrganisation(
                someMinimalOrganisationRequest()
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                           .firstName("some-fname")
                           .lastName("some-lname")
                           .email(email)
                           .build())
                .build());

        Map<String, Object> emailResponse = professionalApiClient.retrieveLegacyPbaNumbersByUserEmail(email
                .toLowerCase());
        assertThat(emailResponse.get("payment_accounts")).asList().isNotEmpty();
        assertThat(emailResponse.get("payment_accounts")).asList().contains(pbaNumber.toUpperCase());
    }

    @Test
    public void can_retrieve_no_payment_numbers_if_no_payment_account_associated_with_user_email() {

        String email = randomAlphabetic(10) + "@pbasearch.test".toLowerCase();

        OrganisationCreationRequest request =  someMinimalOrganisationRequest()
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email(email)
                        .build())
                .build();
        professionalApiClient.createOrganisation(request);
        Map<String, Object> emailResponse = professionalApiClient.retrieveLegacyPbaNumbersByUserEmail(email
                .toLowerCase());
        assertThat(emailResponse.get("payment_accounts")).asList().isEmpty();

    }

}
