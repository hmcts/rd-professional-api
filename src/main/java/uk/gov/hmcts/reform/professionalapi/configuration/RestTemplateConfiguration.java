package uk.gov.hmcts.reform.professionalapi.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestOperations restOperations(
        JsonMapper jsonMapper
    ) {
        return restTemplate(jsonMapper);
    }

    @Bean
    public RestTemplate restTemplate(
        JsonMapper jsonMapper
    ) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate
            .getMessageConverters()
            .add(0, jacksonJsonHttpMessageConverter(jsonMapper));

        return restTemplate;
    }

    @Bean
    public JacksonJsonHttpMessageConverter jacksonJsonHttpMessageConverter(
        JsonMapper jsonMapper
    ) {
        return new JacksonJsonHttpMessageConverter(jsonMapper);
    }
}
