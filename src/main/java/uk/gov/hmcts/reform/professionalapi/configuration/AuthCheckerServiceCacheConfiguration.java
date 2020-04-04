package uk.gov.hmcts.reform.professionalapi.configuration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "auth.checker.cache")
public class AuthCheckerServiceCacheConfiguration {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CacheProperties {
        private int ttlInSeconds;
        private int maximumSize;
    }

    private CacheProperties service = new CacheProperties(60, 100);
}
