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

    @Value("a20c3cf7-1fb4-4bcf-89ec-963c05a13f71")
    public String clientSecret;

    @Value("${test.user.password}")
    public String testUserPassword;

    @Value("https://idam-api.aat.platform.hmcts.net")
    public String idamApiUrl;

    @Value("${idam.auth.redirectUrl}")
    public String oauthRedirectUrl;

    @Value("${idam.auth.clientId:rd-professional-api}")
    public String clientId;

    @Value("${scope-name}")
    protected String scope;

}
