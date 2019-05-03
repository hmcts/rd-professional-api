package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.PbaAccountCreationRequest.aPbaPaymentAccount;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.UserCreationRequest.aUserCreationRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import uk.gov.hmcts.reform.professionalapi.domain.entities.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.util.ProfessionalReferenceDataClient;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;

public class CreateOrganisationWithPaymentAccountForSuperuserTest extends Service2ServiceEnabledIntegrationTest {

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private ProfessionalUserRepository professionalUserRepository;

    @Autowired
    private PaymentAccountRepository paymentAccountRepository;

    private ProfessionalReferenceDataClient professionalReferenceDataClient;

    @Autowired
    private ContactInformationRepository contactInformationRepository;

    @Before
    public void setUp() {
        professionalReferenceDataClient = new ProfessionalReferenceDataClient(port);
        contactInformationRepository.deleteAll();
        professionalUserRepository.deleteAll();
        paymentAccountRepository.deleteAll();
        organisationRepository.deleteAll();
    }

    @Ignore
    @Test
    public void persists_organisation_with_valid_pbaAccount_super_user_contact_Info() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .pbaAccounts(asList(aPbaPaymentAccount()
                        .pbaNumber("pbaNumber-1")
                        .build()))
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
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
