package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.PbaAccountCreationRequest.aPbaPaymentAccount;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;

public class CreateOrganisationWithPaymentAccountForSuperuserTest extends Service2ServiceEnabledIntegrationTest {

    @Test
    public void persists_organisation_with_valid_pbaAccount_super_user_contact_Info() {
        String prefix = UUID.randomUUID().toString();
        List<String> paymentAccounts = new ArrayList<>();
        paymentAccounts.add("pba123");

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .paymentAccounts(paymentAccounts)
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email(String.format("%s@somewhere.com", prefix))
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1").build()))
                .build();

        Map<String, Object> response = professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");

        List<PaymentAccount> persistedPaymentAccounts = paymentAccountRepository.findAll();


        assertThat(response.get("http_status")).asString().contains("201");
        assertThat(persistedPaymentAccounts.size()).isEqualTo(1);
        assertThat(persistedPaymentAccounts.get(0).getOrganisation().getOrganisationIdentifier().toString())
                .isEqualTo(orgIdentifierResponse);
    }

    @Test
    public void persists_and_returns_400_user_email_is_not_unique() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id-number")
                .sraRegulated(Boolean.FALSE)
                .companyUrl("company-url")
                .companyNumber("companyn")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX 1234567890")
                                .dxExchange("dxExchange").build()))
                        .build()))
                .build();
        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        OrganisationCreationRequest organisationCreationRequest2 = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id-number1")
                .sraRegulated(Boolean.FALSE)
                .companyUrl("company-url")
                .companyNumber("company")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX 1234567890")
                                .dxExchange("dxExchange").build()))
                        .build()))
                .build();
        Map<String, Object> response2 =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);
        assertThat(response2.get("http_status")).isEqualTo("400");
    }
}