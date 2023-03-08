package uk.gov.hmcts.reform.professionalapi.util;


import org.springframework.security.oauth2.jwt.JwtDecoder;

public class JwtDecoderMockBuilder extends AuthorizationEnabledIntegrationTest {

    public static void resetJwtDecoder() {
        jwtDecoder = null;
    }

    public static synchronized JwtDecoder getJwtDecoder() {
        return jwtDecoder;
    }
}
