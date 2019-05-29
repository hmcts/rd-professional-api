package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.PbaAccountCreationRequest.aPbaPaymentAccount;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.utils.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

import uk.gov.hmcts.reform.professionalapi.controller.request.PbaAccountCreationRequest;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
public class LegacyPbaNumbersRetrieveByUserEmailTest extends FunctionalTestSuite {

    @Test
    public void can_retrieve_payment_numbers_by_user_email() {

        String email = randomAlphabetic(10) + "@pbasearch.test";
        String pbaNumber =  "pba" + randomAlphabetic(10);
        List<PbaAccountCreationRequest> pbas = Arrays.asList(aPbaPaymentAccount().pbaNumber(pbaNumber).build());
        professionalApiClient.createOrganisation(
                someMinimalOrganisationRequest()
                .pbaAccounts(pbas)
                .superUser(aUserCreationRequest()
                           .firstName("some-fname")
                           .lastName("some-lname")
                           .email(email)
                           .build())
                .build());

        Map<String, Object> emailResponse = professionalApiClient.retrieveLegacyPbaNumbersByUserEmail(email);
        assertThat(emailResponse.get("payment_accounts")).asList().isNotEmpty();
        assertThat(emailResponse.get("payment_accounts")).asList().contains(pbaNumber);
    }

    @Test
    public void can_retrieve_no_payment_numbers_if_no_payment_account_associated_with_user_email() {

        String email = randomAlphabetic(10) + "@pbasearch.test";
        professionalApiClient.createOrganisation(
                someMinimalOrganisationRequest()
                        .superUser(aUserCreationRequest()
                                .firstName("some-fname")
                                .lastName("some-lname")
                                .email(email)
                                .build())
                        .build());

        Map<String, Object> emailResponse = professionalApiClient.retrieveLegacyPbaNumbersByUserEmail(email);
        assertThat(emailResponse.get("payment_accounts")).asList().isEmpty();

    }

}
