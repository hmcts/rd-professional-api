package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class UpdateStatusResponseTest {

    @Test
    public void updateStatusResponseTest() {

        UpdateStatusResponse updateStatusResponse1 = new UpdateStatusResponse() {};

        String idamStatusCode = "Coded";
        String idamMessage = "Message";

        UpdateStatusResponse updateStatusResponse = new UpdateStatusResponse(idamStatusCode, idamMessage);

        assertThat(updateStatusResponse.getIdamStatusCode()).isEqualTo("Coded");
        assertThat(updateStatusResponse.getIdamMessage()).isEqualTo("Message");

    }

}

