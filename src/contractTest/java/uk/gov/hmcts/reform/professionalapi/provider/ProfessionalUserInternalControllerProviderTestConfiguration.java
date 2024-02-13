package uk.gov.hmcts.reform.professionalapi.provider;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.professionalapi.controller.internal.ProfessionalUserInternalController;
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
public class ProfessionalUserInternalControllerProviderTestConfiguration extends ProviderTestConfiguration {

    @MockBean
    protected OrganisationService organisationService;

    @MockBean
    protected ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    FeatureToggleService featureToggleService;

    @MockBean
    ServiceAuthFilter serviceAuthFilter;

    @MockBean
    UserConfiguredAccessRepository userConfiguredAccessRepository;

    @MockBean
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

    @MockBean
    OrganisationRepository organisationRepository;

    @MockBean
    UserAttributeRepository userAttributeRepository;

    @MockBean
    PrdEnumRepository prdEnumRepository;

    @MockBean
    UserAttributeServiceImpl userAttributeService;

    @MockBean
    OrganisationMfaStatusRepository organisationMfaStatusRepository;

    @MockBean
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
    public ProfessionalUserInternalController professionalUserInternalController() {
        return new ProfessionalUserInternalController();
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
