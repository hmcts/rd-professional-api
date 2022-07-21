package uk.gov.hmcts.reform.professionalapi.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "feign.allow")
@Getter
@AllArgsConstructor
public class FeignHeaderConfig {
    private final List<String> headers;
}
