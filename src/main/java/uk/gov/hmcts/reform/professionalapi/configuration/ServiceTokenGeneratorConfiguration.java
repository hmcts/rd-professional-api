package uk.gov.hmcts.reform.professionalapi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

@Configuration
@Lazy
public class ServiceTokenGeneratorConfiguration {

    @Bean
    public AuthTokenGenerator authTokenGenerator(
            ApplicationConfiguration config,
            ServiceAuthorisationApi serviceAuthorisationApi) {
        return AuthTokenGeneratorFactory
                .createDefaultGenerator(
                        config.getS2sSecret(),
                        config.getS2sMicroService(),
                        serviceAuthorisationApi);
    }
}
