package uk.gov.hmcts.reform.professionalapi.wiremock;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.nimbusds.jwt.SignedJWT;
import org.jspecify.annotations.NonNull;
import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;

import java.text.ParseException;

import static java.lang.String.format;

public class IdamResponseTransformer extends ResponseTransformer {
    @Override
    public Response transform(
            Request request,
            Response response,
            FileSource files,
            Parameters parameters) {

        String token = extractBearerToken(request.getHeader("Authorization"));

        UserTokenInfo tokenInfo = getUserIdAndRoleFromToken(token);

        String formattedResponse = format(
                response.getBodyAsString(),
                tokenInfo.userId(),
                tokenInfo.userId(),
                tokenInfo.role()
        );

        return Response.Builder.like(response)
                .but()
                .body(formattedResponse)
                .build();
    }

    @Override
    public String getName() {
        return "external_user-token-response";
    }

    public boolean applyGlobally() {
        return false;
    }

    public static UserTokenInfo getUserIdAndRoleFromToken(String token) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(token);

            String[] parts = getParts(signedJwt);

            return new UserTokenInfo(
                    parts[0], // role
                    parts[1]  // userId
            );

        } catch (ParseException e) {
            throw new InvalidTokenException(
                    "Unable to parse JWT token",
                    e
            );
        }
    }

    private static @NonNull String[] getParts(SignedJWT signedJwt) throws ParseException {
        String subject = signedJwt.getJWTClaimsSet().getSubject();

        if (subject == null || subject.isBlank()) {
            throw new InvalidTokenException(
                    "Token did not return a 'sub' claim"
            );
        }

        String[] parts = subject.trim().split("\\s+", 2);

        if (parts.length != 2) {
            throw new InvalidTokenException(
                    "Token 'sub' must contain role and userId"
            );
        }
        return parts;
    }

    private static String extractBearerToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            throw new InvalidTokenException("Authorization header is missing");
        }

        if (!authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new InvalidTokenException(
                    "Authorization header must use Bearer authentication"
            );
        }

        String token = authorization.substring(7).trim();

        if (token.isEmpty()) {
            throw new InvalidTokenException("Bearer token is missing");
        }

        return token;
    }
}
