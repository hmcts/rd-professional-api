package uk.gov.hmcts.reform.professionalapi.provider;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.professionalapi.controller.internal.OrganisationInternalController;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationByProfileIdsRequestValidator;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;

@TestConfiguration
public class OrganisationalInternalControllerV1ProviderTestConfiguration extends ProviderTestConfiguration {

    @MockBean
    protected OrganisationServiceImpl organisationService;

    @MockBean
    OrganisationByProfileIdsRequestValidator organisationByProfileIdsRequestValidator;


    @Bean
    @Primary
    protected OrganisationInternalController organisationInternalController() {
        return new OrganisationInternalController(organisationByProfileIdsRequestValidator);
    }

}
