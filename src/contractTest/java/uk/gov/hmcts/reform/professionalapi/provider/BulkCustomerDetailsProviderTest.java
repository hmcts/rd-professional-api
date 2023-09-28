package uk.gov.hmcts.reform.professionalapi.provider;


import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.internal.BulkCustomerDetailsInternalController;
import uk.gov.hmcts.reform.professionalapi.controller.request.BulkCustomerRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.ProfessionalUserReqValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UpdateOrganisationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.response.BulkCustomerOrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.BulkCustomerDetails;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.repository.BulkCustomerDetailsRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationMfaStatusRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.MfaStatusService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.UserAccountMapService;
import uk.gov.hmcts.reform.professionalapi.service.UserAttributeService;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;

import java.util.UUID;

import static java.util.Objects.nonNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.when;


@ExtendWith(SpringExtension.class)
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
        host = "${PACT_BROKER_URL:localhost}",
        port = "${PACT_BROKER_PORT:9292}", consumerVersionSelectors = {
        @VersionSelector(tag = "Dev")})
@ContextConfiguration(classes = {BulkCustomerDetailsInternalController.class})
@Provider("referenceData_bulkCustomerDetails")
@TestPropertySource(locations = "/application-contract.yaml")
public class BulkCustomerDetailsProviderTest {

    public static final String ORG_NAME = "Org-Name";
    public static final String SRA_ID = "sra-id";
    public static final String COMPANY_NUMBER = "companyN";
    public static final String COMPANY_URL = "www.org.com";
    public static final String PBA_NUMBER = "PBA1234567";


    @Autowired
    BulkCustomerDetailsInternalController bulkCustomerDetailsInternalController;

    @MockBean
    BulkCustomerDetailsRepository bulkCustomerDetailsRepository;

    @MockBean
    UserAccountMapService userAccountMapService;

    @MockBean
    BulkCustomerRequest bulkCustomerRequest;


    @MockBean
    OrganisationCreationRequestValidator organisationCreationRequestValidator;

    @MockBean
    ProfessionalUserReqValidator professionalUserReqValidator;

    @MockBean
    OrganisationIdentifierValidatorImpl organisationIdentifierValidator;

    @MockBean
    OrganisationServiceImpl organisationService;

    @MockBean
    UpdateOrganisationRequestValidator updateOrganisationRequestValidator;

    @MockBean
    PrdEnumService prdEnumService;

    @MockBean
    protected ProfessionalUserService professionalUserService;

    @MockBean
    ProfessionalUserRepository professionalUserRepository;

    @MockBean
    protected UserProfileUpdateRequestValidator userProfileUpdateRequestValidator;

    @MockBean
    protected PaymentAccountService paymentAccountService;

    @MockBean
    OrganisationRepository organisationRepository;


    @MockBean
    PaymentAccountRepository paymentAccountRepository;

    @MockBean
    DxAddressRepository dxAddressRepository;

    @MockBean
    ContactInformationRepository contactInformationRepository;

    @MockBean
    PrdEnumRepository prdEnumRepository;

    @MockBean
    UserAttributeService userAttributeService;

    @MockBean
    OrganisationMfaStatusRepository organisationMfaStatusRepository;

    @MockBean
    PaymentAccountValidator paymentAccountValidator;

    @MockBean
    UserProfileFeignClient userProfileFeignClient;

    @MockBean
    MfaStatusService mfaStatusService;

    @MockBean
    IdamRepository idamRepository;

    @Value("${resendInviteEnabled}")
    private boolean resendInviteEnabled;

    @MockBean
    BulkCustomerOrganisationsDetailResponse response;

    @Bean
    @Primary
    protected OrganisationServiceImpl organisationService() {
        return new OrganisationServiceImpl();
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }

    @BeforeEach
    void before(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        testTarget.setControllers(
                bulkCustomerDetailsInternalController);
        if (nonNull(context)) {
            context.setTarget(testTarget);
        }

    }


    @State("Organisation Details for Bulk Customer")
    public void toReturnOrganisationalServiceDetails() throws JsonProcessingException {
        BulkCustomerDetails bulkCustomerDetails = getOrganisationDetailsForBulkCustomer();

        when(organisationService.retrieveOrganisationDetailsForBulkCustomer(anyString(), anyString()))
                            .thenReturn(new BulkCustomerOrganisationsDetailResponse(bulkCustomerDetails));
        when(bulkCustomerDetailsRepository.findByBulkCustomerId(anyString(), anyString()))
                            .thenReturn(bulkCustomerDetails);

    }

    private BulkCustomerRequest getbulkCustomerRequest() {
        return BulkCustomerRequest.abulkCustomerRequest()
                        .bulkCustomerId("BulkcustId")
                        .idamId("sidamId")
                        .build();

    }

    private BulkCustomerDetails getOrganisationDetailsForBulkCustomer() {
        Organisation organisation = new Organisation(ORG_NAME, OrganisationStatus.ACTIVE, SRA_ID,
                COMPANY_NUMBER, false, COMPANY_URL);
        organisation.setSraRegulated(true);
        organisation.setOrganisationIdentifier("someOrganisationIdentifier");
        organisation.setId(UUID.fromString("c5e5c75d-cced-4e57-97c8-e359ce33a855"));


        BulkCustomerDetails bulkCustomerDetails = new BulkCustomerDetails();
        bulkCustomerDetails.setOrganisation(organisation);
        bulkCustomerDetails.setSidamId("sidamId");
        bulkCustomerDetails.setPbaNumber(PBA_NUMBER);
        bulkCustomerDetails.setId(UUID.randomUUID());
        bulkCustomerDetails.setBulkCustomerId("BulkcustId");

        return bulkCustomerDetails;
    }
}


