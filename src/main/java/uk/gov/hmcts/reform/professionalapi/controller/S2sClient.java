package uk.gov.hmcts.reform.professionalapi.controller;

import static java.lang.String.format;

import com.warrenstrange.googleauth.GoogleAuthenticator;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;

@Slf4j
@Component
public class S2sClient {

    @Value("${idam.s2s-auth.totp_secret}")
    protected String s2sSecret;

    @Value("${idam.s2s-auth.microservice}")
    protected String s2sMicroServiceName;

    @Value("${idam.s2s-auth.url}")
    protected String s2sUrl;

    @Autowired
    private ServiceAuthorisationApi serviceAuthorisationApi;

    private final GoogleAuthenticator authenticator = new GoogleAuthenticator();

    public String generateS2S() {
        final String oneTimePassword = format("%06d", authenticator.getTotpPassword(s2sSecret));

        Map<String, String> signInDetails = new HashMap<>();
        signInDetails.put("microservice", s2sMicroServiceName);
        signInDetails.put("oneTimePassword", oneTimePassword);

        return serviceAuthorisationApi.serviceToken(signInDetails);
    }
}