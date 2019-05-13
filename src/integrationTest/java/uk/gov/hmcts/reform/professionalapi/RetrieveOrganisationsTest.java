package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest.aContactInformationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.DxAddressCreationRequest.dxAddressCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest.anOrganisationCreationRequest;
import static uk.gov.hmcts.reform.professionalapi.controller.request.PbaAccountCreationRequest.aPbaPaymentAccount;
import static uk.gov.hmcts.reform.professionalapi.controller.request.UserCreationRequest.aUserCreationRequest;

import java.util.Arrays;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.persistence.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.util.ProfessionalReferenceDataClient;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;

@Slf4j
public class RetrieveOrganisationsTest extends Service2ServiceEnabledIntegrationTest {

    @Autowired
    private OrganisationRepository organisationRepository;

    @Autowired
    private ProfessionalUserRepository professionalUserRepository;

    @Autowired
    private PaymentAccountRepository paymentAccountRepository;

    @Autowired
    private ContactInformationRepository contactInformationRepository;

    @Autowired
    private DxAddressRepository dxAddressRepository;

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
    public void persists_and_returns_all_organisations_details() {

        OrganisationCreationRequest organisationCreationRequest = anOrganisationCreationRequest()
                .name("some-org-name")
                .sraId("sra-id1")
                .sraRegulated(Boolean.FALSE)
                .companyUrl("company-url")
                .companyNumber("company")
                .superUser(aUserCreationRequest()
                        .firstName("some-fname")
                        .lastName("some-lname")
                        .email("someone@somewhere.com")
                        .build())
                .pbaAccounts(Arrays.asList(aPbaPaymentAccount()
                .pbaNumber("pbaNumber")
                .build()))
                .contactInformation(Arrays.asList(aContactInformationCreationRequest()
                        .addressLine1("addressLine1")
                        .addressLine2("addressLine2")
                        .addressLine3("addressLine3")
                        .country("country")
                        .county("county")
                        .townCity("town-city")
                        .postCode("post-code-test")
                        .dxAddress(Arrays.asList(dxAddressCreationRequest()
                                .dxNumber("DX 1234567890")
                                .dxExchange("dxExchange").build()))
                        .build()))
                .build();

        Map<String, Object> response =
              professionalReferenceDataClient.createOrganisation(organisationCreationRequest);

        String orgIdentifierResponse = (String) response.get("organisationIdentifier");
        assertThat(orgIdentifierResponse).isNotEmpty();
        Map<String, Object> orgResponse =
                professionalReferenceDataClient.retrieveAllOrganisationDetailsTest();
        assertThat(orgResponse.get("organisations")).isNotNull();
        assertThat(orgResponse.get("http_status").toString().contains("OK"));
        log.info("RetrieveOrganisationsTest:Received response to retrieve an organisation details...");
    }
}
