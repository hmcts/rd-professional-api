package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.spring.spring7.Spring7MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.professionalapi.BaseProviderTest;

@TestPropertySource(locations = "/application-contract.yaml")
public abstract class MockMvcProviderTest extends BaseProviderTest {

    Spring7MockMvcTestTarget testTarget = new Spring7MockMvcTestTarget();

    @BeforeEach
    void before(PactVerificationContext context) {
        this.setController();
        if (context != null) {
            context.setTarget(testTarget);
        }
    }

    abstract void setController();
}
