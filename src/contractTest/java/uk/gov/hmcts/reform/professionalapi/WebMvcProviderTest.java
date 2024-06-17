package uk.gov.hmcts.reform.professionalapi;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.professionalapi.service.UserAttributeService;

public class WebMvcProviderTest extends BaseProviderTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UserAttributeService userAttributeService;

    @BeforeEach
    void before(PactVerificationContext context) {
        if (context != null) {
            context.setTarget(new MockMvcTestTarget(mockMvc));
        }
    }
}
