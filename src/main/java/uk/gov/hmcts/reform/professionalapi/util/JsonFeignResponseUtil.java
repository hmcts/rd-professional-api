package uk.gov.hmcts.reform.professionalapi.util;

import static java.time.LocalDateTime.now;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.UNAUTHORISED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.UNSUCCESSFUL_AUTHENTICATION;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;

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
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static ResponseEntity<Object> toResponseEntity(Response response, Object clazz) {
        Optional<Object> payload;

        if (response.status() == 401) {
            payload = Optional.of(new ErrorResponse(UNAUTHORISED, UNSUCCESSFUL_AUTHENTICATION, now().toString()));
        } else {
            payload = decode(response, clazz);
        }

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
                    .equalsIgnoreCase("content-length"))) {
                responseEntityHeaders.put(e.getKey(), new ArrayList<>(e.getValue()));
            }
        });


        return responseEntityHeaders;
    }
}
