package uk.gov.hmcts.reform.professionalapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PbaResponseTest {

    @Test
    void test_pbaResponseNoArgsConstructor() {
        PbaResponse pbaResponse = new PbaResponse();

        String success = "Success";
        String statusCode = "200";

        pbaResponse.setStatusMessage(success);
        pbaResponse.setStatusCode(statusCode);

        assertThat(pbaResponse.getStatusCode()).isEqualTo(statusCode);
        assertThat(pbaResponse.getStatusMessage()).isEqualTo(success);
    }

    @Test
    void test_pbaResponseAllArgsConstructor() {
        String success = "Success";
        String statusCode = "200";

        PbaResponse pbaResponse = new PbaResponse(statusCode, success);

        assertThat(pbaResponse.getStatusCode()).isEqualTo(statusCode);
        assertThat(pbaResponse.getStatusMessage()).isEqualTo(success);
    }
}