package uk.gov.hmcts.reform.professionalapi.configuration.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "idam.security")
public class IdamSecurityProperties {
    private List<String> allowedIssuers = new ArrayList<>();
}