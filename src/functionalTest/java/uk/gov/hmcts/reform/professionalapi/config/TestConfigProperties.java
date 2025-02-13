package uk.gov.hmcts.reform.professionalapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.lib.config.TestConfig;

@Getter
@Setter
@Configuration
public class TestConfigProperties implements TestConfig {

    @Value("${oauth2-client-secret}")
    public String clientSecret;

    @Value("${test.user.password}")
    public String testUserPassword;

    @Value("${idam.api.url}")
    public String idamApiUrl;

    @Value("${idam.auth.redirectUrl}")
    public String oauthRedirectUrl;

    @Value("${idam.auth.clientId:xuiwebapp}")
    public String clientId;

    @Value("${scope-name}")
    protected String scope;

}
