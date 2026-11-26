package uk.gov.hmcts.reform.professionalapi.util;

import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ExtendWith(MockitoExtension.class)
class JsonFeignResponseUtilTest {

    @Test
    @SuppressWarnings("unchecked")
    void testDecode() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);

        Response response = Response.builder().status(200).reason("OK").headers(header)
                .body("{\"userIdentifier\": 1}", UTF_8).request(mock(Request.class)).build();
        Optional<Object> createUserProfileResponseOptional = JsonFeignResponseUtil.decode(response,
                ProfessionalUsersResponse.class);

        assertThat(createUserProfileResponseOptional).isNotEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_Decode_fails_with_ioException() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);

        Response.Body bodyMock = mock(Response.Body.class);
        Response response = Response.builder().status(200).reason("OK").headers(header).body(bodyMock)
                .request(mock(Request.class)).build();

        try {
            when(bodyMock.asReader(Charset.defaultCharset())).thenThrow(new IOException());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Optional<Object> createUserProfileResponseOptional = JsonFeignResponseUtil.decode(response,
                ProfessionalUsersResponse.class);
        assertThat(createUserProfileResponseOptional).isEmpty();
    }

    @Test
    void test_convertHeaders() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>(Arrays.asList("gzip", "request-context", "x-powered-by",
                "content-length"));
        header.put("content-encoding", list);

        HttpHeaders responseHeader = JsonFeignResponseUtil.convertHeaders(header);
        assertThat(responseHeader.headerNames()).isNotEmpty();

        Collection<String> emptylist = new ArrayList<>();
        header.put("content-encoding", emptylist);
        HttpHeaders responseHeader1 = JsonFeignResponseUtil.convertHeaders(header);

        assertThat(responseHeader1.get("content-encoding")).isEmpty();
    }

    @Test
    void test_toResponseEntity_with_payload_not_empty() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>(Arrays.asList("a", "b"));
        header.put("content-encoding", list);

        Response response = Response.builder().status(200).reason("OK").headers(header)
                .body("{\"userIdentifier\": 1}", UTF_8).request(mock(Request.class)).build();
        ResponseEntity<Object> entity =
                JsonFeignResponseUtil.toResponseEntity(response, ProfessionalUsersResponse.class);

        assertThat(entity).isNotNull();
        assertThat(entity.getStatusCode().value()).isEqualTo(200);
        assertThat(entity.getHeaders().headerNames()).isNotEmpty();
        assertThat(((ProfessionalUsersResponse) entity.getBody()).getUserIdentifier()).isEqualTo("1");
    }

    @Test
    void test_privateConstructor() throws Exception {
        Constructor<JsonFeignResponseUtil> constructor = JsonFeignResponseUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }

    @Test
    void test_decode_null_response_returns_empty() {
        assertThat(JsonFeignResponseUtil.decode(null, ProfessionalUsersResponse.class)).isEmpty();
    }

    @Test
    void test_decode_null_body_returns_empty() {
        Response response = Response.builder().status(200).reason("OK")
                .headers(Map.of())
                .request(mock(Request.class)).build();

        assertThat(JsonFeignResponseUtil.decode(response, ProfessionalUsersResponse.class)).isEmpty();
    }

    @Test
    void test_decode_empty_body_returns_empty() {
        Map<String, Collection<String>> header = new HashMap<>();
        Response response = Response.builder().status(200).reason("OK").headers(header)
                .body("", UTF_8).request(mock(Request.class)).build();

        assertThat(JsonFeignResponseUtil.decode(response, ProfessionalUsersResponse.class)).isEmpty();
    }

    @Test
    void test_toResponseEntity_null_response_returns_500_without_npe() {
        assertThatCode(() -> JsonFeignResponseUtil.toResponseEntity(null, ProfessionalUsersResponse.class))
                .doesNotThrowAnyException();

        ResponseEntity<Object> entity =
                JsonFeignResponseUtil.toResponseEntity(null, ProfessionalUsersResponse.class);

        assertThat(entity.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(entity.getBody()).isNull();
    }

    @Test
    void test_toResponseEntity_null_headers_returns_entity_without_npe() {
        Response response = mock(Response.class);
        when(response.status()).thenReturn(204);
        when(response.headers()).thenReturn(null);
        when(response.body()).thenReturn(null);

        ResponseEntity<Object> entity =
                JsonFeignResponseUtil.toResponseEntity(response, ProfessionalUsersResponse.class);

        assertThat(entity.getStatusCode().value()).isEqualTo(204);
        assertThat(entity.getBody()).isNull();
        assertThat(entity.getHeaders().headerNames()).isEmpty();
    }

    @Test
    void test_convertHeaders_null_map_returns_empty_headers() {
        HttpHeaders headers = JsonFeignResponseUtil.convertHeaders(null);

        assertThat(headers.headerNames()).isEmpty();
    }

    @Test
    void test_convertHeaders_skips_null_key_and_handles_null_values() {
        Map<String, Collection<String>> header = new HashMap<>();
        header.put(null, List.of("ignored"));
        header.put("x-custom", null);
        header.put("request-context", List.of("drop-me"));
        header.put("content-encoding", List.of("gzip"));

        HttpHeaders responseHeader = JsonFeignResponseUtil.convertHeaders(header);

        assertThat(responseHeader.containsHeader("content-encoding")).isTrue();
        assertThat(responseHeader.get("content-encoding")).containsExactly("gzip");
        assertThat(responseHeader.containsHeader("x-custom")).isTrue();
        assertThat(responseHeader.get("x-custom")).isEmpty();
        assertThat(responseHeader.containsHeader("request-context")).isFalse();
    }
}
