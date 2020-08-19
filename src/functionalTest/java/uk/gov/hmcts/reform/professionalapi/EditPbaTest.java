package uk.gov.hmcts.reform.professionalapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.createJurisdictions;
import static uk.gov.hmcts.reform.professionalapi.helper.OrganisationFixtures.someMinimalOrganisationRequest;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;

@RunWith(SpringIntegrationSerenityRunner.class)
@ActiveProfiles("functional")
public class EditPbaTest extends AuthorizationFunctionalTest {

    @Test
    public void can_edit_active_organisation_payment_accounts_by_orgId() {
        String email = randomAlphabetic(10) + "@pbasearch.test".toLowerCase();
        String pbaNumber = "PBA" + randomAlphabetic(7);

        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add(pbaNumber);

        OrganisationCreationRequest request = someMinimalOrganisationRequest()
                .paymentAccount(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email(email)
                        .jurisdictions(createJurisdictions())
                        .build())
                .build();

        Map<String, Object> response = professionalApiClient.createOrganisation(request);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();
        request.setStatus("ACTIVE");

        professionalApiClient.updateOrganisation(request, hmctsAdmin, orgIdentifierResponse);

        Map<String, Object> orgResponse =
                professionalApiClient.retrievePaymentAccountsByEmail(email.toLowerCase(), hmctsAdmin);

        assertThat(orgResponse).isNotEmpty();
        responseValidate(orgResponse);

        Set<String> paymentAccountsEdit = new HashSet<>();
        paymentAccountsEdit.add(pbaNumber);
        paymentAccountsEdit.add("PBA" + randomAlphabetic(7));
        paymentAccountsEdit.add("PBA" + randomAlphabetic(7));
        PbaEditRequest pbaEditRequest = new PbaEditRequest();
        pbaEditRequest.setPaymentAccounts(paymentAccountsEdit);

        Map<String, Object> pbaResponse =
                professionalApiClient.editPbaAccountsByOrgId(pbaEditRequest, orgIdentifierResponse, hmctsAdmin);

        assertThat(pbaResponse).isNotEmpty();
    }


    private void responseValidate(Map<String, Object> orgResponse) {

        orgResponse.forEach((k, v) -> {

            if ("organisationIdentifier".equals(k) && "http_status".equals(k)
                    && "name".equals(k) && "status".equals(k)
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
