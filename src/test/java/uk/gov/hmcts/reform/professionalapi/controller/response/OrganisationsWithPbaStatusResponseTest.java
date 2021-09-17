package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;

public class OrganisationsWithPbaStatusResponseTest {

    @Test
    public void testOrganisationsWithPbaStatusResponse() {
        PaymentAccount pba1 = new PaymentAccount();
        pba1.setPbaStatus(ACCEPTED);
        pba1.setPbaNumber("PBA123456");
        pba1.setCreated(LocalDateTime.now());
        pba1.setLastUpdated(LocalDateTime.now());
        List<PaymentAccount> pbas = Collections.singletonList(pba1);

        OrganisationsWithPbaStatusResponse organisations =
                new OrganisationsWithPbaStatusResponse("ABCDEFG7", OrganisationStatus.ACTIVE, pbas);

        assertEquals("ABCDEFG7", organisations.getOrganisationIdentifier());
        assertEquals(1, organisations.getPbaNumbers().size());
        assertEquals(OrganisationStatus.ACTIVE, organisations.getOrganisationStatus());
    }

    @Test
    public void testOrganisationsWithPbaStatusResponseSetter() {
        PaymentAccount pba1 = new PaymentAccount();
        pba1.setPbaStatus(ACCEPTED);
        pba1.setPbaNumber("PBA123456");
        pba1.setCreated(LocalDateTime.now());
        pba1.setLastUpdated(LocalDateTime.now());
        List<PaymentAccount> pbas = Collections.singletonList(pba1);
        List<FetchPbaByStatusResponse> pbaResponse = new ArrayList<>();

        OrganisationsWithPbaStatusResponse organisations = new OrganisationsWithPbaStatusResponse();
        organisations.setOrganisationStatus(OrganisationStatus.ACTIVE);
        organisations.setPbaNumbers(pbaResponse);
        organisations.setOrganisationIdentifier("ABCDEFG7");

        assertEquals("ABCDEFG7", organisations.getOrganisationIdentifier());
        assertEquals(0, organisations.getPbaNumbers().size());
        assertEquals(OrganisationStatus.ACTIVE, organisations.getOrganisationStatus());
    }
}
