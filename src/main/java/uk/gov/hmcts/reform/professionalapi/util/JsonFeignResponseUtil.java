package uk.gov.hmcts.reform.professionalapi.util;

import feign.Response;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.ConstructorDetector;
import tools.jackson.databind.json.JsonMapper;

import java.io.BufferedReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;


@SuppressWarnings("unchecked")
public class JsonFeignResponseUtil {
    private static final JsonMapper json = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            // Match JacksonConfiguration: prefer no-arg when present (Boot 3 parity)
            .constructorDetector(
                    ConstructorDetector.DEFAULT.withAllowImplicitWithDefaultConstructor(false))
            .build();

    private JsonFeignResponseUtil() {

    }

    public static Optional<Object> decode(Response response, Object clazz) {
        try {
            if (response == null) {
                return Optional.empty();
            }
            Response.Body body = response.body();
            if (body == null) {
                return Optional.empty();
            }
            Reader rawReader = body.asReader(Charset.defaultCharset());
            if (rawReader == null) {
                return Optional.empty();
            }
            // Buffer so empty-body probe works; Feign InputStreamReaders often lack mark support
            BufferedReader reader = rawReader instanceof BufferedReader buffered
                    ? buffered
                    : new BufferedReader(rawReader);
            reader.mark(1);
            int read = reader.read();
            if (read < 0) {
                return Optional.empty();
            }
            reader.reset();
            return Optional.of(json.readValue(reader, (Class<Object>) clazz));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static ResponseEntity<Object> toResponseEntity(Response response, Object clazz) {
        Optional<Object> payload = decode(response, clazz);

        return ResponseEntity
                .status(HttpStatus.valueOf(response.status()))
                .headers(convertHeaders(response.headers()))
                .body(payload.orElse(null));
    }

    public static HttpHeaders convertHeaders(Map<String, Collection<String>> responseHeaders) {
        HttpHeaders responseEntityHeaders = new HttpHeaders();
        responseHeaders.entrySet().stream().forEach(e -> {
            if (!(e.getKey().equalsIgnoreCase("request-context") || e.getKey()
                    .equalsIgnoreCase("x-powered-by") || e.getKey()
                    .equalsIgnoreCase("content-length") || e.getKey()
                    .equalsIgnoreCase("transfer-encoding"))) {
                responseEntityHeaders.put(e.getKey(), new ArrayList<>(e.getValue()));
            }
        });

        return responseEntityHeaders;
    }
}
