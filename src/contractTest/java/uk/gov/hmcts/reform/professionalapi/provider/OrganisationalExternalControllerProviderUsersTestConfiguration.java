package uk.gov.hmcts.reform.professionalapi.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.professionalapi.controller.external.OrganisationExternalController;
import uk.gov.hmcts.reform.professionalapi.controller.external.ProfessionalExternalUserController;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.repository.BulkCustomerDetailsRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrgAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationMfaStatusRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserConfiguredAccessRepository;
import uk.gov.hmcts.reform.professionalapi.service.FeatureToggleService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.UserAttributeService;
import uk.gov.hmcts.reform.professionalapi.service.impl.MfaStatusServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.PaymentAccountServiceImpl;

@Configuration
public class OrganisationalExternalControllerProviderUsersTestConfiguration extends ProviderTestConfiguration {

    @MockitoBean
    protected ProfessionalUserService professionalUserService;

    @MockitoBean
    protected ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    protected UserProfileUpdateRequestValidator userProfileUpdateRequestValidator;

    @MockitoBean
    UserConfiguredAccessRepository userConfiguredAccessRepository;

    @MockitoBean
    FeatureToggleService featureToggleService;

    @MockitoBean
    ServiceAuthFilter serviceAuthFilter;

    @MockitoBean
    OrganisationRepository organisationRepository;

    @MockitoBean
    BulkCustomerDetailsRepository bulkCustomerDetailsRepository;

    @MockitoBean
    PaymentAccountRepository paymentAccountRepository;
    @MockitoBean
    DxAddressRepository dxAddressRepository;
    @MockitoBean
    ContactInformationRepository contactInformationRepository;
    @MockitoBean
    PrdEnumRepository prdEnumRepository;
    @MockitoBean
    UserAttributeService userAttributeService;
    @MockitoBean
    OrganisationMfaStatusRepository organisationMfaStatusRepository;

    @MockitoBean
    OrgAttributeRepository orgAttributeRepository;

    @Bean
    @Primary
    protected OrganisationServiceImpl organisationService() {
        return new OrganisationServiceImpl();
    }

    @Bean
    @Primary
    public PaymentAccountService paymentAccountService() {
        return new PaymentAccountServiceImpl(configuration, userProfileFeignClient,
            emf, professionalUserRepository, organisationService(),
            userAccountMapService, paymentAccountRepository);
    }

    @Bean
    @Primary
    public OrganisationExternalController organisationExternalController() {
        return new OrganisationExternalController();
    }

    @Bean
    @Primary
    public ProfessionalExternalUserController professionalExternalUserController() {
        return new ProfessionalExternalUserController();
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
