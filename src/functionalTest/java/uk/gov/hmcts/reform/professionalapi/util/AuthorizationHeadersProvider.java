package uk.gov.hmcts.reform.professionalapi.util;

import io.restassured.http.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Service
public class AuthorizationHeadersProvider {

    @Autowired
    private AuthTokenGenerator serviceAuthTokenGenerator;

    public Header getServiceAuthorization() {
        return new Header("ServiceAuthorization", serviceAuthTokenGenerator.generate());
    }
}
