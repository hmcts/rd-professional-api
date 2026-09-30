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

import java.time.LocalDateTime;
import java.time.ZoneId;
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

    public static String generateAuthToken(final String issuer,
                                           final boolean isExpired,
                                           final String userId,
                                           final String role) {

        final LocalDateTime now = LocalDateTime.now();

        final LocalDateTime issuedAt = isExpired
                ? now.minusHours(2)
                : now.minusSeconds(60);

        final LocalDateTime expiresAt = isExpired
                ? now.minusHours(1)
                : now.plusHours(1);

        final JWTClaimsSet.Builder claimsBuilder =
                getJwtClaimsBuilder(issuedAt, expiresAt)
                        .subject(role + " " + userId)
                        .audience(role);

        if (issuer != null) {
            claimsBuilder.issuer(issuer);
        }

        try {
            final JWSHeader header =
                    new JWSHeader.Builder(JWSAlgorithm.RS256)
                            .keyID(TEST_RSA_JWK.getKeyID())
                            .build();

            final SignedJWT signedJwt =
                    new SignedJWT(header, claimsBuilder.build());

            signedJwt.sign(new RSASSASigner(TEST_RSA_JWK));

            return signedJwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate JWT", e);
        }
    }

    private static JWTClaimsSet.Builder getJwtClaimsBuilder(
            final LocalDateTime issuedAt,
            final LocalDateTime expiresAt) {

        final ZoneId zoneId = ZoneId.systemDefault();

        return new JWTClaimsSet.Builder()
                .issueTime(Date.from(issuedAt.atZone(zoneId).toInstant()))
                .claim(TOKEN_NAME, ACCESS_TOKEN)
                .expirationTime(Date.from(expiresAt.atZone(zoneId).toInstant()));
    }

    public static String generateS2SToken(String serviceName) {
        return Jwts.builder()
                .setSubject(serviceName)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.encode("AA"))
                .compact();
    }
}

