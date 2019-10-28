package uk.gov.hmcts.reform.professionalapi.sample;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@PactTestFor(providerName = "citizenservice", port = "8888")
@SpringBootTest({
                // overriding provider address
                "citizenservice.ribbon.listOfServers: localhost:8888"
})
public class CitizenServiceConsumerTest {

    @Autowired
    private CitizenClient citizenClient;

    @Pact(state = "provider accepts a new citizen", provider = "citizenservice", consumer = "citizenclient")
    RequestResponsePact createCitizenPact(PactDslWithProvider builder) {
        // @formatter:off
        return builder
                .given("provider accepts a new citizen")
                .uponReceiving("a request to POST a citizen")
                .path("/citizen-service/citizens")
                .method("POST")
                .willRespondWith()
                .status(201)
                .matchHeader("Content-Type", "application/json")
                .body(new PactDslJsonBody()
                        .integerType("id", 42))
                        .toPact();
        // @formatter:on
    }

    @Pact(state = "citizen 42 exists", provider = "citizenservice", consumer = "citizenclient")
    RequestResponsePact updateCitizenPact(PactDslWithProvider builder) {
        // @formatter:off
        return builder
                        .given("citizen 42 exists")
                        .uponReceiving("a request to PUT a citizen")
                          .path("/citizen-service/citizens/42")
                          .method("PUT")
                        .willRespondWith()
                          .status(200)
                          .matchHeader("Content-Type", "application/json")
                          .body(new PactDslJsonBody()
                                            .stringType("firstName", "Jon")
                                            .stringType("lastName", "Snow"))
                        .toPact();
        // @formatter:on
    }


    @Test
    @PactTestFor(pactMethod = "createCitizenPact")
    void verifyCreateCitizenPact() {
        Citizen citizen = new Citizen();
        citizen.setFirstName("Jon");
        citizen.setLastName("Snow");
        IdObject id = citizenClient.createCitizen(citizen);
        assertThat(id.getId()).isEqualTo(42);
    }

    @Test
    @PactTestFor(pactMethod = "updateCitizenPact")
    void verifyUpdateCitizenPact() {
        Citizen citizen = new Citizen();
        citizen.setFirstName("Jon");
        citizen.setLastName("Snow");
        Citizen updateCitizen = citizenClient.updateCitizen(42L, citizen);
        assertThat(updateCitizen.getFirstName()).isEqualTo("Jon");
        assertThat(updateCitizen.getLastName()).isEqualTo("Snow");
    }

}
