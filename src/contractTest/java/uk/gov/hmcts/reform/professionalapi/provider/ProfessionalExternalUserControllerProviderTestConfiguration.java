package uk.gov.hmcts.reform.professionalapi.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.professionalapi.controller.external.ProfessionalExternalUserController;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.UserProfileUpdateRequestValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationMfaStatusRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserConfiguredAccessRepository;
import uk.gov.hmcts.reform.professionalapi.service.FeatureToggleService;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.impl.MfaStatusServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.PaymentAccountServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserAttributeServiceImpl;

@Configuration
public class ProfessionalExternalUserControllerProviderTestConfiguration extends ProviderTestConfiguration {

    @MockitoBean
    protected OrganisationService organisationService;

    @MockitoBean
    protected ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    FeatureToggleService featureToggleService;

    @MockitoBean
    ServiceAuthFilter serviceAuthFilter;

    @MockitoBean
    UserConfiguredAccessRepository userConfiguredAccessRepository;

    @MockitoBean
    UserProfileUpdateRequestValidator userProfileUpdateRequestValidator;

    @Primary
    @Bean
    protected ProfessionalUserService professionalUserService() {
        return new ProfessionalUserServiceImpl(organisationRepository,
                professionalUserRepository,
                userAttributeRepository,
                prdEnumRepository,
                userAttributeService,
                userProfileFeignClient,
                userConfiguredAccessRepository,
                userProfileUpdateRequestValidator);
    }

    @MockitoBean
    OrganisationRepository organisationRepository;

    @MockitoBean
    UserAttributeRepository userAttributeRepository;

    @MockitoBean
    PrdEnumRepository prdEnumRepository;

    @MockitoBean
    UserAttributeServiceImpl userAttributeService;

    @MockitoBean
    OrganisationMfaStatusRepository organisationMfaStatusRepository;

    @MockitoBean
    PaymentAccountRepository paymentAccountRepository;

    @Bean
    @Primary
    public PaymentAccountService paymentAccountService() {
        return new PaymentAccountServiceImpl(configuration, userProfileFeignClient,
            emf, professionalUserRepository, organisationService,
            userAccountMapService, paymentAccountRepository);
    }

    @Bean
    @Primary
    public ProfessionalExternalUserController professionalExternalUserController() {
        return new ProfessionalExternalUserController();
    }

    @Bean
    @Primary
    protected UserProfileUpdateRequestValidator userProfileUpdateRequestValidator() {
        return new UserProfileUpdateRequestValidatorImpl();
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
