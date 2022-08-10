package uk.gov.hmcts.reform.professionalapi.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

@Slf4j
@Configuration
@Lazy
public class ServiceTokenGeneratorConfiguration {

    @Bean
    public AuthTokenGenerator authTokenGenerator(
            ApplicationConfiguration config,
            ServiceAuthorisationApi serviceAuthorisationApi) {
        log.info("This is Service Authorisation" + config.getS2sSecret() + config.getS2sMicroService())
                ;
        return AuthTokenGeneratorFactory
                .createDefaultGenerator(
                        config.getS2sSecret(),
                        config.getS2sMicroService(),
                        serviceAuthorisationApi);
    }
}
