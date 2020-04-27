package uk.gov.hmcts.reform.professionalapi.util;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;

@Slf4j
public final class JwtTokenUtil {

    private static final String SUBJECT = "sub";

    private JwtTokenUtil() {
    }

    /**
     * Generate JWT Signed Token.
     * @param issuer    Issuer
     * @param ttlMillis Time to live
     * @return String
     */
    public static String generateToken(String issuer, long ttlMillis, String userId, String role) {
        final long nowMillis = System.currentTimeMillis();

        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .subject(role + " " + userId)
                .issueTime(new Date())
                .issuer(issuer)
                .audience(role)
                .claim("tokenName", "access_token");

        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.expirationTime(exp);
        }

        SignedJWT signedJwt = null;
        try {
            signedJwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(KeyGenUtil.getRsaJwk().getKeyID()).build(),
                    builder.build());
            signedJwt.sign(new RSASSASigner(KeyGenUtil.getRsaJwk()));
        } catch (JOSEException e) {
            log.error("error while creating bearer token : " + (e.getMessage()));
        }
        return signedJwt.serialize();
    }

    public static String decodeJwtToken(String jwtToken) {

        String[] splitString = jwtToken.split("\\.");
        String base64EncodedBody = splitString[1];
        Base64 base64Url = new Base64(true);
        return new String(base64Url.decode(base64EncodedBody));

    }

    /**
     * Fetch userId and role from the token body.TokenBody is in json format and it fetches key 'sub'
     * to get comma separated value containing userId and role
     *
     * @param tokenBody tokenBody in string format
     * @return List containing userId and role
     */
    public static LinkedList getUserIdAndRoleFromToken(String tokenBody) {
        String[] tokenElements = tokenBody.split(",");
        List<String> elements = Arrays.asList(tokenElements).stream()
                .filter(element -> element.contains(SUBJECT))
                .map(subElement -> subElement.split(":")[1].replace("\"", ""))
                .collect(Collectors.toList());

        if (elements.isEmpty()) {
            throw new InvalidTokenException("Token did not returned 'subject' element");
        }

        String[] tokenisedSubValue = elements.get(0).split(" ");
        LinkedList tokenResult = new LinkedList();
        tokenResult.add(tokenisedSubValue[0]);
        tokenResult.add(tokenisedSubValue[1]);
        return tokenResult;
    }
}

