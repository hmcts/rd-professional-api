package uk.gov.hmcts.reform.professionalapi.provider;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.professionalapi.controller.internal.OrganisationInternalController;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.DxAddressRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrgAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationMfaStatusRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.UserAttributeService;
import uk.gov.hmcts.reform.professionalapi.service.impl.MfaStatusServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;

@Configuration
public class OrganisationalInternalControllerProviderTestConfiguration extends ProviderTestConfiguration {

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
    OrgAttributeRepository orgAttributeRepository;

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
