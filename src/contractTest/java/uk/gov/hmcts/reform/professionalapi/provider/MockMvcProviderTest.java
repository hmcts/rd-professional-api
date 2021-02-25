package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(locations = "/application-contract.yaml")
public abstract class MockMvcProviderTest extends BaseProviderTest {

    MockMvcTestTarget testTarget = new MockMvcTestTarget();

    @BeforeEach
    void before(PactVerificationContext context) {
        this.setController();
        if (context != null) {
            context.setTarget(testTarget);
        }
    }

    abstract void setController();
}
