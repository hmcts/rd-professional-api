package uk.gov.hmcts.reform.professionalapi.provider;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.professionalapi.controller.external.OrganisationExternalControllerV2;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrgAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.impl.MfaStatusServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;

@TestConfiguration
public class OrganisationalExternalControllerV2ProviderTestConfiguration extends ProviderTestConfiguration {

    @MockitoBean
    protected ProfessionalUserService professionalUserService;

    @MockitoBean
    protected UserProfileUpdateRequestValidator userProfileUpdateRequestValidator;


    @MockitoBean
    OrgAttributeRepository orgAttributeRepository;

    @MockitoBean
    protected OrganisationServiceImpl organisationService;


    @Bean
    @Primary
    protected OrganisationExternalControllerV2 organisationExternalControllerV2() {
        return new OrganisationExternalControllerV2();
    }

    @Bean
    @Primary
    public PaymentAccountService paymentAccountService() {
        return Mockito.mock(PaymentAccountService.class);
    }

    @Bean
    @Primary
    protected MfaStatusServiceImpl mfaStatusService() {
        return new MfaStatusServiceImpl();
    }

    @MockitoBean
    protected PaymentAccountRepository paymentAccountRepository;

    @MockitoBean
    ContactInformationRepository contactInformationRepositoryMock;

}
