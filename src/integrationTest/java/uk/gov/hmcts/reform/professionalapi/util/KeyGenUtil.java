package uk.gov.hmcts.reform.professionalapi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class KeyGenUtil {

    private static RSAKey rsaJwk;
    private static final String KEY_ID = "23456789";

    private KeyGenUtil() {
    }

    public static RSAKey getRsaJwk() throws JOSEException {
        if (rsaJwk == null) {
            rsaJwk = new RSAKeyGenerator(2048)
                    .keyID(KEY_ID)
                    .generate();
        }
        return rsaJwk;
    }

    public static String getDynamicJwksResponse() throws JOSEException, JsonProcessingException {
        RSAKey rsaKey = KeyGenUtil.getRsaJwk();
        Map<String, List<Map<String, Object>>> body = new LinkedHashMap<>();
        List<Map<String, Object>> keyList = new ArrayList<>();
        keyList.add(rsaKey.toJSONObject());
        body.put("keys", keyList);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(body);
    }

}
