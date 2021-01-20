package uk.gov.hmcts.reform.professionalapi.provider;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.professionalapi.controller.external.ProfessionalExternalUserController;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.UserProfileUpdateRequestValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.impl.PaymentAccountServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserAttributeServiceImpl;

@TestConfiguration
public class ProfessionalExternalUserControllerProviderTestConfiguration extends ProviderTestConfiguration {

    @MockBean
    protected OrganisationService organisationService;

    @Primary
    @Bean
    protected ProfessionalUserService professionalUserService() {
        return new ProfessionalUserServiceImpl(organisationRepository,
            professionalUserRepository,
            userAttributeRepository,
            prdEnumRepository,
            userAttributeService,
            userProfileFeignClient);
    }

    @MockBean
    OrganisationRepository organisationRepository;

    @MockBean
    UserAttributeRepository userAttributeRepository;

    @MockBean
    PrdEnumRepository prdEnumRepository;

    @MockBean
    UserAttributeServiceImpl userAttributeService;

    @Bean
    @Primary
    public PaymentAccountService paymentAccountService() {
        return new PaymentAccountServiceImpl(configuration, userProfileFeignClient,
            emf, professionalUserRepository, organisationService,
            userAccountMapService);
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

    ;


    @MockBean
    protected JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;
}
