package uk.gov.hmcts.reform.professionalapi.infrastructure.security;

import java.util.Map;

public interface AccessTokenDecoder {

    Map<String, String> decode(
        String accessToken
    );
}
