package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.PbaAccountCreationRequest.aPbaPaymentAccount;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

import java.util.Arrays;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.persistence.*;
import uk.gov.hmcts.reform.professionalapi.service.impl.PaymentAccountServiceImpl;
import uk.gov.hmcts.reform.professionalapi.util.ProfessionalReferenceDataClient;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;

public class FindPaymentAccountsByEmailTest extends Service2ServiceEnabledIntegrationTest {

    @Autowired
    private OrganisationRepository organisationRepository;
    @Autowired
    private ProfessionalUserRepository professionalUserRepository;
    @Autowired
    private ContactInformationRepository contactInformationRepository;
    @Autowired
    private DxAddressRepository dxAddressRepository;
    @Autowired
    PaymentAccountRepository paymentAccountRepository;
    @Autowired
    PaymentAccountServiceImpl paymentAccountService;

    private ProfessionalReferenceDataClient professionalReferenceDataClient;

    @Before
    public void setUp() {
        professionalReferenceDataClient = new ProfessionalReferenceDataClient(port);
        dxAddressRepository.deleteAll();
        contactInformationRepository.deleteAll();
        professionalUserRepository.deleteAll();
        paymentAccountRepository.deleteAll();
        organisationRepository.deleteAll();
    }

    @Test
    public void get_request_returns_correct_payment_accounts() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-")
                .pbaAccounts(asList(aPbaPaymentAccount()
                        .pbaNumber("pbaNumber-1")
                        .build()))
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("some@email.com")
                        .build())
                .contactInformation(Arrays.asList(aContactInformationCreationRequest().addressLine1("addressLine1").build()))
                .build();

        Map<String, Object> organisationResponse =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);



        Organisation persistedOrganisation = paymentAccountService.findPaymentAccountsByEmail("some@email.com");

        assertEquals("some-org-", persistedOrganisation.getName());
        assertThat(persistedOrganisation.getPaymentAccounts().contains("pbaNumber-1"));
    }

    @Test
    public void returns_404_when_email_not_found() {
        Map<String, Object> response =
                professionalReferenceDataClient.findPaymentAccountsByEmail("wrong@email.com");

        assertThat(response.get("http_status")).isEqualTo("404");
    }

}
