package uk.gov.hmcts.reform.professionalapi.provider;


import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.professionalapi.controller.internal.OrganisationInternalController;
import uk.gov.hmcts.reform.professionalapi.controller.request.BulkCustomerRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.BulkCustomerOrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationMfaStatusRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.service.MfaStatusService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.UserAttributeService;
import uk.gov.hmcts.reform.professionalapi.service.impl.MfaStatusServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;

@Configuration
public class BulkCustomerDetailsProviderTestConfiguration extends ProviderTestConfiguration {


    @MockBean
    BulkCustomerRequest bulkCustomerRequest;

    @MockBean
    protected ProfessionalUserService professionalUserService;

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
    MfaStatusService mfaStatusService;

    @MockBean
    BulkCustomerOrganisationsDetailResponse response;

    @Bean
    @Primary
    protected OrganisationInternalController organisationInternalController() {
        return new OrganisationInternalController();
    }

    @Bean
    @Primary
    protected OrganisationServiceImpl organisationService() {
        return new OrganisationServiceImpl();
    }

    @Bean
    @Primary
    public PactJwtGrantedAuthoritiesConverter pactJwtGrantedAuthoritiesConverter() {
        return new PactJwtGrantedAuthoritiesConverter(idamRepository);
    }


    @Bean
    @Primary
    protected MfaStatusServiceImpl mfaStatusService() {
        return new MfaStatusServiceImpl();
    }


}
