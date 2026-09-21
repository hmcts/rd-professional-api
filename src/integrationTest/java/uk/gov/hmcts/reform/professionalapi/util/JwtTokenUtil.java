package uk.gov.hmcts.reform.professionalapi.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN;
import static uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter.TOKEN_NAME;

@Slf4j
public final class JwtTokenUtil {

    private static final RSAKey TEST_RSA_JWK;

    static {
        try {
            TEST_RSA_JWK = KeyGenUtil.getRsaJwk();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    private JwtTokenUtil() {
    }

    public static String generateAuthToken(String issuer,
                                           boolean isExpired,
                                           String userId,
                                           String role) {

        Instant now = Instant.now();

        Instant issuedAt = isExpired
                ? now.minus(2, ChronoUnit.HOURS)
                : now.minusSeconds(60);

        Instant expiresAt = isExpired
                ? now.minus(1, ChronoUnit.HOURS)
                : now.plusSeconds(3600);

        JWTClaimsSet.Builder claimsBuilder =
                getJwtClaimsBuilder(Date.from(issuedAt), Date.from(expiresAt))
                        .subject(role + " " + userId).audience(role);
        ;

        if (issuer != null) {
            claimsBuilder.issuer(issuer);
        }

        try {
            JWSHeader header =
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID(TEST_RSA_JWK.getKeyID())
                            .build();

            SignedJWT signedJwt = new SignedJWT(header, claimsBuilder.build());

            signedJwt.sign(new RSASSASigner(TEST_RSA_JWK));
            return signedJwt.serialize();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static JWTClaimsSet.Builder getJwtClaimsBuilder(Date issuedAt,
                                                            Date expiresAt) {
        return new JWTClaimsSet.Builder()
                .issueTime(issuedAt)
                .claim(TOKEN_NAME, ACCESS_TOKEN)
                .expirationTime(expiresAt);
    }

    public static String generateS2SToken(String serviceName) {
        return Jwts.builder()
                .setSubject(serviceName)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.encode("AA"))
                .compact();
    }
}

