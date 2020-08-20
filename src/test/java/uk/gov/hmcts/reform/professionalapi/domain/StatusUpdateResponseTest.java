package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StatusUpdateResponseTest {

    private String idamStatusCode = "Coded";
    private String idamMessage = "Message";

    @Test
    public void test_updateStatusResponse() {
        StatusUpdateResponse statusUpdateResponse = new StatusUpdateResponse(idamStatusCode, idamMessage);

        assertThat(statusUpdateResponse.getIdamStatusCode()).isEqualTo(idamStatusCode);
        assertThat(statusUpdateResponse.getIdamMessage()).isEqualTo(idamMessage);
    }
}

