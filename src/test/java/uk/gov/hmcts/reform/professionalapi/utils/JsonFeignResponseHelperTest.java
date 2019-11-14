package uk.gov.hmcts.reform.professionalapi.utils;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import feign.Request;
import feign.Response;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.util.JsonFeignResponseHelper;

public class JsonFeignResponseHelperTest {


    @Test
    @SuppressWarnings("unchecked")
    public void testDecode() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        Request request = mock(Request.class);
        Response response = Response.builder().status(200).reason("OK").headers(header).body("{\"userIdentifier\": 1}", UTF_8).request(request).build();
        Optional<ProfessionalUsersResponse> createUserProfileResponseOptional = JsonFeignResponseHelper.decode(response, ProfessionalUsersResponse.class);
        assertThat(createUserProfileResponseOptional).isNotEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDecode_fails_with_ioException() {

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        Request request = mock(Request.class);
        Response.Body bodyMock = mock(Response.Body.class);
        Response response = Response.builder().status(200).reason("OK").headers(header).body(bodyMock).request(request).build();
        try {
            when(bodyMock.asInputStream()).thenThrow(new IOException());
            when(bodyMock.asReader()).thenThrow(new IOException());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Optional<ProfessionalUsersResponse> createUserProfileResponseOptional = JsonFeignResponseHelper.decode(response, ProfessionalUsersResponse.class);
        assertThat(createUserProfileResponseOptional).isEmpty();
    }

    @Test
    public void test_convertHeaders() {

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>(Arrays.asList(new String[]{"gzip",  "request-context", "x-powered-by", "content-length"}));
        header.put("content-encoding", list);
        MultiValueMap<String, String> responseHeader = JsonFeignResponseHelper.convertHeaders(header);
        assertThat(responseHeader).isNotEmpty();

        Collection<String> emptylist = new ArrayList<>();
        header.put("content-encoding", emptylist);
        MultiValueMap<String, String> responseHeader1 = JsonFeignResponseHelper.convertHeaders(header);
        assertThat(responseHeader1.get("content-encoding")).isEmpty();

    }

    @Test
    public void test_toResponseEntity_with_payload_not_empty() {

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>(Arrays.asList(new String[]{"a", "b"}));
        header.put("content-encoding", list);
        Request request = mock(Request.class);
        Response response = Response.builder().status(200).reason("OK").headers(header).body("{\"userIdentifier\": 1}", UTF_8).request(request).build();
        ResponseEntity entity = JsonFeignResponseHelper.toResponseEntity(response, ProfessionalUsersResponse.class);
        assertThat(entity).isNotNull();
        assertThat(entity.getStatusCode().value()).isEqualTo(200);
        assertThat(entity.getHeaders()).isNotEmpty();
        assertThat(((ProfessionalUsersResponse)entity.getBody()).getUserIdentifier()).isEqualTo("1");
    }

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<JsonFeignResponseHelper> constructor = JsonFeignResponseHelper.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }

}
