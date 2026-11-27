package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@Provider("referenceData_organisationalInternal")
@PactFolder("src/contractTest/resources/pacts/active-search")
class ActiveOrganisationSearchProviderTest {
    private final ActiveOrganisationSearchProviderStates search = new ActiveOrganisationSearchProviderStates();

    @BeforeEach
    void target(PactVerificationContext context) {
        var target = new MockMvcTestTarget();
        target.setControllers(search.controller());
        context.setTarget(target);
        context.addStateChangeHandlers(search);
    }

    @AfterEach
    void stopWorkers() {
        search.stopWorkers();
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }
}
