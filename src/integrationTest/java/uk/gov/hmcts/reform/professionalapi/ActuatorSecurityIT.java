package uk.gov.hmcts.reform.professionalapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.professionalapi.util.AuthorizationEnabledIntegrationTest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

class ActuatorSecurityIT extends AuthorizationEnabledIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldAllowAnonymousAccessToHealthEndpoint() throws Exception {
        MvcResult result = mockMvc.perform(get("/health")).andReturn();
        int status = result.getResponse().getStatus();
        assertTrue(status == 200 || status == 503,
                "Expected /health to return 200 or 503, but got: " + status);
    }

    @Test
    void shouldNotExposeLoggersEndpointAnonymously() throws Exception {
        MvcResult result = mockMvc.perform(get("/loggers")).andReturn();
        int status = result.getResponse().getStatus();
        assertTrue(status == 401 || status == 404,
                "Expected /loggers to be protected (401) or disabled (404), but got: " + status);
    }
}
