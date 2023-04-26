package uk.gov.hmcts.reform.professionalapi.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WireMockUtil {

    private WireMockUtil() {
    }

    public static ObjectMapper getObjectMapper() {

        ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;

    }
}
