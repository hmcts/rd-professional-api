package uk.gov.hmcts.reform.professionalapi.provider;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.professionalapi.controller.internal.ProfessionalUserInternalControllerV2;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.UsersInOrganisationsByOrganisationIdentifiersRequestValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.repository.*;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.impl.*;

@Configuration
public class ProfessionalUserInternalControllerV2ProviderTestConfiguration extends ProviderTestConfiguration {
    @MockBean
    protected UsersInOrganisationsByOrganisationIdentifiersRequestValidatorImpl usersInOrganisationsByOrganisationIdentifiersRequestValidatorImpl;

    @MockBean
    protected ProfessionalUserServiceImpl professionalUserService;

    @MockBean
    protected OrganisationServiceImpl organisationService;

    @MockBean
    protected PaymentAccountRepository paymentAccountRepository;

    @MockBean
    OrganisationRepository organisationRepository;

    @MockBean
    ContactInformationRepository contactInformationRepositoryMock;

    @MockBean
    UserAttributeRepository userAttributeRepository;

    @MockBean
    PrdEnumRepository prdEnumRepository;

    @MockBean
    UserAttributeServiceImpl userAttributeService;

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

    @Bean
    @Primary
    protected ProfessionalUserInternalControllerV2 professionalUserInternalControllerV2() {
        return new ProfessionalUserInternalControllerV2();
    }

    @Bean
    @Primary
    public PaymentAccountService paymentAccountService() {
        return new PaymentAccountServiceImpl(configuration, userProfileFeignClient,
                emf, professionalUserRepository, organisationService,
                userAccountMapService, paymentAccountRepository);
    }

    @Bean
    @Primary
    protected MfaStatusServiceImpl mfaStatusService() {
        return new MfaStatusServiceImpl();
    }
}
