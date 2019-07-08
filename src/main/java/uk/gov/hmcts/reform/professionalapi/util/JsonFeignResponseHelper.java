package uk.gov.hmcts.reform.userprofileapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class JsonFeignResponseHelper {
    private static final ObjectMapper json = new ObjectMapper();

    private JsonFeignResponseHelper() {

    }

    public static <T> Optional<T> decode(Response response, Class<T> clazz) {
        if (response.status() >= 200 && response.status() < 300 && clazz != null) {
            try {
                return Optional.of(json.readValue(response.body().asReader(), clazz));
            } catch (IOException e) {
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }

    public static <U> ResponseEntity<U> toResponseEntity(Response response, Class<U> clazz) {
        Optional<U> payload = decode(response, clazz);

        return new ResponseEntity<U>(
                payload.orElse(null),//didn't find a way to feed body with original content if payload is empty
                convertHeaders(response.headers()),
                HttpStatus.valueOf(response.status()));
    }

    public static MultiValueMap<String, String> convertHeaders(Map<String, Collection<String>> responseHeaders) {
        MultiValueMap<String, String> responseEntityHeaders = new LinkedMultiValueMap<>();
        responseHeaders.entrySet().stream().forEach(e ->
                responseEntityHeaders.put(e.getKey(), new ArrayList<>(e.getValue())));
        return responseEntityHeaders;
    }
}
