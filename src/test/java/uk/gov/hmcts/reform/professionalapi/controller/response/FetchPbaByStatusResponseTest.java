package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;

class FetchPbaByStatusResponseTest {

    @Test
    void testFetchPbaByStatusResponse() {
        FetchPbaByStatusResponse pba = new FetchPbaByStatusResponse();
        pba.setPbaNumber("PBA123446");
        pba.setStatus(ACCEPTED.toString());
        pba.setStatusMessage("Edited by admin");
        pba.setDateAccepted("dateAccepted");
        pba.setDateCreated("dateCreated");

        assertEquals("PBA123446", pba.getPbaNumber());
        assertEquals(ACCEPTED.toString(), pba.getStatus());
        assertEquals("Edited by admin", pba.getStatusMessage());
        assertEquals("dateCreated", pba.getDateCreated());
        assertEquals("dateAccepted", pba.getDateAccepted());
    }

    @Test
    void testFetchPbaByStatusResponseNoArgs() {
        PaymentAccount paymentAccount = new PaymentAccount();
        LocalDateTime time = LocalDateTime.now();
        paymentAccount.setPbaStatus(ACCEPTED);
        paymentAccount.setStatusMessage("Edited by admin");
        paymentAccount.setPbaNumber("PBA123456");
        paymentAccount.setCreated(time);
        paymentAccount.setLastUpdated(time);
        FetchPbaByStatusResponse pba = new FetchPbaByStatusResponse(paymentAccount);

        assertEquals("PBA123456", pba.getPbaNumber());
        assertEquals(ACCEPTED.toString(), pba.getStatus());
        assertEquals("Edited by admin", pba.getStatusMessage());
        assertEquals(time.toString(), pba.getDateCreated());
        assertEquals(time.toString(), pba.getDateAccepted());
    }
}
