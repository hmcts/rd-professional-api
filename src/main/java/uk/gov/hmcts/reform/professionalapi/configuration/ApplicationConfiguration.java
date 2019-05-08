package uk.gov.hmcts.reform.professionalapi.configuration;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ApplicationConfiguration {

    private final String s2sSecret;
    private final String s2sMicroService;
    private final String s2sUrl;

    public ApplicationConfiguration(@Value("${idam.s2s-auth.totp_secret}") String s2sSecret,
                                    @Value("${idam.s2s-auth.microservice}") String s2sMicroService,
                                    @Value("${idam.s2s-auth.url}") String s2sUrl) {
        this.s2sSecret = s2sSecret;
        this.s2sMicroService = s2sMicroService;
        this.s2sUrl = s2sUrl;
        log.info("Configured S2S secret: " + s2sSecret.substring(0, 2) + "************" + s2sSecret.substring(14));
        log.info("Configured S2S microservice: " + s2sMicroService);
        log.info("Configured S2S URL: " + s2sUrl);
    }

    public String getS2sSecret() {
        return s2sSecret;
    }

    public String getS2sMicroService() {
        return s2sMicroService;
    }

    public String getS2sUrl() {
        return s2sUrl;
    }
}
