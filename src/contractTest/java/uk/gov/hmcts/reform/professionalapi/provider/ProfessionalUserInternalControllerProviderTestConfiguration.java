package uk.gov.hmcts.reform.professionalapi.provider;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UserProfileUpdateRequestValidator;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserConfiguredAccessRepository;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserAttributeServiceImpl;

@Configuration
public class ProfessionalUserInternalControllerProviderTestConfiguration extends ProviderTestConfiguration {

    @MockBean
    UserConfiguredAccessRepository userConfiguredAccessRepository;

    @MockBean
    UserProfileUpdateRequestValidator userProfileUpdateRequestValidator;

    @MockBean
    OrganisationRepository organisationRepository;

    @MockBean
    UserAttributeRepository userAttributeRepository;

    @MockBean
    PrdEnumRepository prdEnumRepository;

    @MockBean
    UserAttributeServiceImpl userAttributeService;

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
}
