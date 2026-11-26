package uk.gov.hmcts.reform.professionalapi.provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.ConstructorDetector;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;
import uk.gov.hmcts.reform.professionalapi.service.FeatureToggleService;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;

@TestConfiguration
public class ProviderTestConfiguration {

    @Value("${prd.security.roles.hmcts-admin:}")
    protected String prdAdmin;

    @Value("${prd.security.roles.pui-user-manager:}")
    protected String puiUserManager;

    @Value("${prd.security.roles.pui-organisation-manager:}")
    protected String puiOrgManager;

    @Value("${prd.security.roles.pui-finance-manager}")
    protected String puiFinanceManager;

    @Value("${prd.security.roles.pui-case-manager:}")
    protected String puiCaseManager;

    @Value("${prdEnumRoleType}")
    protected String prdEnumRoleType;

    @Value("${resendInviteEnabled}")
    private boolean resendInviteEnabled;

    @Value("${allowedStatus}")
    private String allowedOrganisationStatus;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Primary
    @Bean(name = "DefaultObjectMapper")
    public JsonMapper defaultObjectMapper() {
        return JsonMapper.builder()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_ABSENT))
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .enable(EnumFeature.READ_ENUMS_USING_TO_STRING)
                .enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
                .enable(EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .constructorDetector(
                        ConstructorDetector.DEFAULT.withAllowImplicitWithDefaultConstructor(false))
                .build();
    }

    @Bean
    public RestTemplate restTemplate(JsonMapper jsonMapper) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate
                .getMessageConverters()
                .add(0, jacksonJsonHttpMessageConverter(jsonMapper));

        return restTemplate;
    }

    @Bean
    public RestOperations restOperations(JsonMapper jsonMapper) {
        return restTemplate(jsonMapper);
    }

    @Bean(name = "httpMessageConverter")
    public JacksonJsonHttpMessageConverter jacksonJsonHttpMessageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonHttpMessageConverter(jsonMapper);
    }

    @Bean
    public JacksonJsonHttpMessageConverter newJsonConvert() {
        return new JacksonJsonHttpMessageConverter(JsonMapper.builder().build());
    }

    @Bean
    @ConditionalOnMissingBean
    public FeatureToggleService featureToggleService() {
        return Mockito.mock(FeatureToggleService.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public PaymentAccountService paymentAccountService() {
        return Mockito.mock(PaymentAccountService.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public OrganisationService organisationService() {
        return Mockito.mock(OrganisationService.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProfessionalUserService professionalUserService() {
        return Mockito.mock(ProfessionalUserService.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public IdamRepository idamRepository() {
        return Mockito.mock(IdamRepository.class);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter(IdamRepository idamRepository) {
        return new PactJwtGrantedAuthoritiesConverter(idamRepository);
    }

}
