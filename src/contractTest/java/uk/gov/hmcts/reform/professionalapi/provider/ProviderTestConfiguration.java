package uk.gov.hmcts.reform.professionalapi.provider;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.professionalapi.configuration.ApplicationConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.ProfessionalUserReqValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UpdateOrganisationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;
import uk.gov.hmcts.reform.professionalapi.service.UserAccountMapService;

import javax.persistence.EntityManagerFactory;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_ENUMS_USING_TO_STRING;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_ENUMS_USING_TO_STRING;

public class ProviderTestConfiguration {

    @MockBean
    ApplicationConfiguration configuration;
    @MockBean
    UserProfileFeignClient userProfileFeignClient;
    @MockBean
    EntityManagerFactory emf;
    @MockBean
    ProfessionalUserRepository professionalUserRepository;
    @MockBean
    UserAccountMapService userAccountMapService;
    @MockBean
    protected PrdEnumService prdEnumService;
    @MockBean
    protected UpdateOrganisationRequestValidator updateOrganisationRequestValidator;
    @MockBean
    protected OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImpl;
    @MockBean
    protected ProfessionalUserReqValidator profExtUsrReqValidator;
    @MockBean
    protected PaymentAccountValidator paymentAccountValidator;

    @MockBean
    public OrganisationCreationRequestValidator organisationCreationRequestValidatorMock;


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
    public ObjectMapper defaultObjectMapper() {
        return new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(READ_ENUMS_USING_TO_STRING, true)
                .configure(WRITE_ENUMS_USING_TO_STRING, true)
                .configure(READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
    }

    @Bean
    @Primary
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
                .featuresToEnable(READ_ENUMS_USING_TO_STRING)
                .featuresToEnable(READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .featuresToEnable(WRITE_ENUMS_USING_TO_STRING)
                .serializationInclusion(JsonInclude.Include.NON_ABSENT);
    }

    @Bean
    public RestTemplate restTemplate(ObjectMapper objectMapper) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate
                .getMessageConverters()
                .add(0, mappingJackson2HttpMessageConverter(objectMapper));

        return restTemplate;
    }

    @Bean
    public RestOperations restOperations(ObjectMapper objectMapper) {
        return restTemplate(objectMapper);
    }

    @Bean(name = "httpMessageConverter")
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }

    @Bean()
    public MappingJackson2HttpMessageConverter newJsonConvert() {
        return new MappingJackson2HttpMessageConverter(new ObjectMapper());
    }

}