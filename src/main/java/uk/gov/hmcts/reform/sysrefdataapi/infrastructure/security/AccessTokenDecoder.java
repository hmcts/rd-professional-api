package uk.gov.hmcts.reform.sysrefdataapi.infrastructure.security;

import java.util.Map;

public interface AccessTokenDecoder {

    Map<String, String> decode(
        String accessToken
    );
}
