package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StatusUpdateResponseTest {

    @Test
    public void updateStatusResponseTest() {

        StatusUpdateResponse statusUpdateResponse1 = new StatusUpdateResponse() {};

        String idamStatusCode = "Coded";
        String idamMessage = "Message";

        StatusUpdateResponse statusUpdateResponse = new StatusUpdateResponse(idamStatusCode, idamMessage);

        assertThat(statusUpdateResponse.getIdamStatusCode()).isEqualTo("Coded");
        assertThat(statusUpdateResponse.getIdamMessage()).isEqualTo("Message");

    }

}

