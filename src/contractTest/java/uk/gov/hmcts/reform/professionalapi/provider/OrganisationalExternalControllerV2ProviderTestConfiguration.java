package uk.gov.hmcts.reform.professionalapi.provider;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.professionalapi.controller.external.OrganisationExternalControllerV2;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrgAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.SingletonOrgTypeRepository;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.impl.MfaStatusServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.PaymentAccountServiceImpl;

@TestConfiguration
public class OrganisationalExternalControllerV2ProviderTestConfiguration extends ProviderTestConfiguration {

    @MockBean
    protected ProfessionalUserService professionalUserService;

    @MockBean
    protected UserProfileUpdateRequestValidator userProfileUpdateRequestValidator;


    @MockBean
    OrgAttributeRepository orgAttributeRepository;

    @MockBean
    protected OrganisationServiceImpl organisationService;

    @MockBean
    protected SingletonOrgTypeRepository singletonOrgTypeRepository;

    @Bean
    @Primary
    protected OrganisationExternalControllerV2 organisationExternalControllerV2() {
        return new OrganisationExternalControllerV2();
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

    @MockBean
    protected PaymentAccountRepository paymentAccountRepository;

    @MockBean
    ContactInformationRepository contactInformationRepositoryMock;

}
