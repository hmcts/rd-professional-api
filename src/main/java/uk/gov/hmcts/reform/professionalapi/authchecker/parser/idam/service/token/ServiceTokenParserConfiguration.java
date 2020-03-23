package uk.gov.hmcts.reform.professionalapi.authchecker.parser.idam.service.token;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;


@Configuration
@Lazy
public class ServiceTokenParserConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "serviceTokenParserHttpClient")
    public HttpClient serviceTokenParserHttpClient() {
        return HttpClients.createDefault();
    }

    @Bean
    public ServiceTokenParser serviceAuthProviderAuthCheckClient(HttpClient serviceTokenParserHttpClient,
                                                                 @Value("${auth.provider.service.client.baseUrl}") String baseUrl) {

        return new HttpComponentsBasedServiceTokenParser(serviceTokenParserHttpClient, baseUrl);
    }

}

