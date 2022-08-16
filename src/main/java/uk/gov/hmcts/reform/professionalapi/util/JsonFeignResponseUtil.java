package uk.gov.hmcts.reform.professionalapi.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;


@SuppressWarnings("unchecked")
public class JsonFeignResponseUtil {
    private static final ObjectMapper json = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonFeignResponseUtil() {

    }

    public static Optional<Object> decode(Response response, Object clazz) {
        try {
            return Optional.of(json.readValue(response.body().asReader(Charset.defaultCharset()),
                    (Class<Object>) clazz));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static ResponseEntity<Object> toResponseEntity(Response response, Object clazz) {
        Optional<Object>  payload = decode(response, clazz);

        return new ResponseEntity<>(
                payload.orElse(null),
                convertHeaders(response.headers()),
                HttpStatus.valueOf(response.status()));
    }

    public static MultiValueMap<String, String> convertHeaders(Map<String, Collection<String>> responseHeaders) {
        MultiValueMap<String, String> responseEntityHeaders = new LinkedMultiValueMap<>();
        responseHeaders.entrySet().stream().forEach(e -> {
            if (!(e.getKey().equalsIgnoreCase("request-context") || e.getKey()
                    .equalsIgnoreCase("x-powered-by") || e.getKey()
                    .equalsIgnoreCase("content-length") || e.getKey()
                    .equalsIgnoreCase("transfer-encoding"))) {
                responseEntityHeaders.put(e.getKey(), new ArrayList<>(e.getValue()));
            }
        });
        responseEntityHeaders.remove("transfer-encoding");
        return responseEntityHeaders;
    }
}
