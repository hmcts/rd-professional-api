package uk.gov.hmcts.reform.professionalapi.provider;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.professionalapi.controller.internal.ProfessionalUserInternalControllerV2;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.UsersInOrganisationsByOrganisationIdentifiersRequestValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.impl.MfaStatusServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.PaymentAccountServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;

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
