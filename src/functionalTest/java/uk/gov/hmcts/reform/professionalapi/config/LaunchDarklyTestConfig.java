package uk.gov.hmcts.reform.professionalapi.config;

import com.launchdarkly.sdk.server.LDClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.lang.System.getenv;

@Configuration
public class LaunchDarklyTestConfig {

    @Bean
    public LDClient ldClient() {
        return new LDClient(getenv("LD_SDK_KEY"));
    }
}
