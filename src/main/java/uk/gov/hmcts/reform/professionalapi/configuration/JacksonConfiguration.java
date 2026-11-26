package uk.gov.hmcts.reform.professionalapi.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.ConstructorDetector;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Jackson 3 (Boot 4) mapper customisation preserving Boot 3 contracts:
 * ISO dates, enum toString, ignore unknown, NON_ABSENT, omitted primitives default,
 * and no-arg + other constructor detection parity.
 */
@Configuration
public class JacksonConfiguration {

    @Bean
    public JsonMapperBuilderCustomizer professionalApiJsonMapperCustomizer() {
        return builder -> builder
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_ABSENT))
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                // Boot 3 / Jackson 2 wrote fields in declaration order, not alphabetical
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .enable(EnumFeature.READ_ENUMS_USING_TO_STRING)
                .enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
                .enable(EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .constructorDetector(
                        ConstructorDetector.DEFAULT.withAllowImplicitWithDefaultConstructor(false));
    }

    @Bean(name = "DefaultObjectMapper")
    public JsonMapper defaultObjectMapper(JsonMapper.Builder builder) {
        return builder.build();
    }
}
