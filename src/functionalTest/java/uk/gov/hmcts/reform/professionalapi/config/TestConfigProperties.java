package uk.gov.hmcts.reform.professionalapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Configuration
public class TestConfigProperties {

    @Autowired
    public Oauth2 oauth2;

    @Value("${test.url}")
    public String baseTestUrl;

    @Value("${generated.user.email.pattern}")
    public String generatedUserEmailPattern;

    @Value("${test.user.password}")
    public String testUserPassword;

    @Value("${idam.api.url}")
    public String idamApiUrl;

}
