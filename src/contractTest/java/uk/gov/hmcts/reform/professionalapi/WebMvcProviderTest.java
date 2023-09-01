package uk.gov.hmcts.reform.professionalapi;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.hmcts.reform.professionalapi.controller.external.OrganisationExternalController;

public class WebMvcProviderTest extends BaseProviderTest {

    MockMvcTestTarget testTarget = new MockMvcTestTarget();

    @BeforeEach
    void before(PactVerificationContext context) {

        if (context != null) {
            context.setTarget(testTarget);
        }

    }
}
