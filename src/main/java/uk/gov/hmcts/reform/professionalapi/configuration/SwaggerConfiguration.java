package uk.gov.hmcts.reform.professionalapi.configuration;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.springdoc.core.GroupedOpenApi;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;



@Configuration
@SecurityScheme(name = "Authorization", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityScheme(name = "ServiceAuthorization", type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER, bearerFormat = "JWT", description = "ServiceAuthorization")
@SecurityScheme(name = "UserEmail", type = SecuritySchemeType.APIKEY, in = SecuritySchemeIn.HEADER)
public class SwaggerConfiguration {

    @Bean
    public GroupedOpenApi publicApi(OperationCustomizer customGlobalHeaders) {
        return GroupedOpenApi.builder()
                .group("rd-professional-api")
                .pathsToMatch("/**")
                .build();
    }


    @Bean
    public OperationCustomizer customGlobalHeaders() {
        return (Operation customOperation, HandlerMethod handlerMethod) -> {
            Parameter serviceAuthorizationHeader = new Parameter()
                    .in(ParameterIn.HEADER.toString())
                    .schema(new StringSchema())
                    .name("ServiceAuthorization")
                    .description("Keyword `Bearer` followed "
                            + "by a service-to-service token for a whitelisted micro-service")
                    .required(true);
            Parameter authorizationHeader = new Parameter()
                    .in(ParameterIn.HEADER.toString())
                    .schema(new StringSchema())
                    .name("Authorization")
                    .description("Authorization token")
                    .required(true);
            Parameter userEmail = new Parameter()
                    .in(ParameterIn.HEADER.toString())
                    .schema(new StringSchema())
                    .name("UserEmail")
                    .description("UserEmail")
                    .required(false);
            customOperation.addParametersItem(authorizationHeader);
            customOperation.addParametersItem(serviceAuthorizationHeader);
            customOperation.addParametersItem(userEmail);
            return customOperation;
        };
    }
}
