package uk.gov.hmcts.reform.sysrefdataapi.util;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Service
public class AuthorizationHeadersProvider {

    @Autowired
    private AuthTokenGenerator serviceAuthTokenGenerator;

    public Headers getServiceAuthorization() {

        return new Headers(
            new Header("ServiceAuthorization", serviceAuthTokenGenerator.generate())
        );
    }
}
