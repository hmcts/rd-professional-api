package uk.gov.hmcts.reform.professionalapi.controller.request;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PbaRequestTest {

    @Test
    void testPbaRequest() {
        PbaUpdateRequest pbaRequest = new PbaUpdateRequest("pbaNumber", "status", "statusMessage");

        assertThat(pbaRequest.getPbaNumber()).isEqualTo("pbaNumber");
        assertThat(pbaRequest.getStatus()).isEqualTo("status");
        assertThat(pbaRequest.getStatusMessage()).isEqualTo("statusMessage");

        pbaRequest.setPbaNumber("PBA1234567");
        pbaRequest.setStatus("ACCEPTED");
        pbaRequest.setStatusMessage("Approved by Admin");

        assertThat(pbaRequest.getPbaNumber()).isEqualTo("PBA1234567");
        assertThat(pbaRequest.getStatus()).isEqualTo("ACCEPTED");
        assertThat(pbaRequest.getStatusMessage()).isEqualTo("Approved by Admin");
    }
}
