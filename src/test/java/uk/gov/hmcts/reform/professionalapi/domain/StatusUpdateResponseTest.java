package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatusUpdateResponseTest {

    private String idamStatusCode = "Coded";
    private String idamMessage = "Message";

    @Test
    void test_updateStatusResponse() {
        StatusUpdateResponse statusUpdateResponse = new StatusUpdateResponse(idamStatusCode, idamMessage);

        assertThat(statusUpdateResponse.getIdamStatusCode()).isEqualTo(idamStatusCode);
        assertThat(statusUpdateResponse.getIdamMessage()).isEqualTo(idamMessage);
    }
}

