package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.PbaAccountCreationRequest.aPbaPaymentAccount;
import static uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.UserCreationRequest.aUserCreationRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.professionalapi.domain.entities.PaymentAccount;
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

    @Before
    public void setUp() {
        professionalReferenceDataClient = new ProfessionalReferenceDataClient(port);
        professionalUserRepository.deleteAll();
        paymentAccountRepository.deleteAll();
        organisationRepository.deleteAll();
    }

    @Test
    public void persists_organisation_with_valid_super_user_payment_account() {

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

        Map<String, Object> response =
                professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");

        List<PaymentAccount> persistedPaymentAccounts = paymentAccountRepository.findAll();


        assertThat(response.get("http_status")).isEqualTo("201");
        assertThat(persistedPaymentAccounts.size()).isEqualTo(1);
        assertThat(persistedPaymentAccounts.get(0).getOrganisation().getOrganisationIdentifier().toString())
                .isEqualTo(orgIdentifierResponse);
    }
}
