package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StatusUpdateResponseTest {

    @Test
    public void test_updateStatusResponse() {
        String idamStatusCode = "Coded";
        String idamMessage = "Message";
        StatusUpdateResponse statusUpdateResponse = new StatusUpdateResponse(idamStatusCode, idamMessage);

        assertThat(statusUpdateResponse.getIdamStatusCode()).isEqualTo(idamStatusCode);
        assertThat(statusUpdateResponse.getIdamMessage()).isEqualTo(idamMessage);
    }
}

