package uk.gov.hmcts.reform.professionalapi.provider;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.professionalapi.controller.external.OrganisationExternalController;
import uk.gov.hmcts.reform.professionalapi.controller.external.OrganisationMfaStatusController;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrgAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationMfaStatusRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.SingletonOrgTypeRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.impl.MfaStatusServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.PaymentAccountServiceImpl;

@TestConfiguration
public class OrganisationalExternalControllerProviderTestConfiguration extends ProviderTestConfiguration {

    @MockBean
    protected ProfessionalUserService professionalUserService;

    @MockBean
    protected UserProfileUpdateRequestValidator userProfileUpdateRequestValidator;

    @MockBean
    protected SingletonOrgTypeRepository singletonOrgTypeRepository;

    @MockBean
    protected OrganisationService organisationService;

    @MockBean
    OrgAttributeRepository orgAttributeRepository;

    @Bean
    @Primary
    public PaymentAccountService paymentAccountService() {
        return new PaymentAccountServiceImpl(configuration, userProfileFeignClient,
            emf, professionalUserRepository, organisationService,
            userAccountMapService, paymentAccountRepository);
    }

    @Bean
    @Primary
    public OrganisationExternalController organisationExternalController() {
        return new OrganisationExternalController();
    }

    @Bean
    @Primary
    public OrganisationMfaStatusController organisationMfaStatusController() {
        return new OrganisationMfaStatusController();
    }

    @MockBean
    protected JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @MockBean
    OrganisationMfaStatusRepository organisationMfaStatusRepository;

    @MockBean
    OrganisationRepository organisationRepository;

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
