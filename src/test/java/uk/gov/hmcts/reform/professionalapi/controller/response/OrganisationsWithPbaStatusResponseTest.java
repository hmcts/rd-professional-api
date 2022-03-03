package uk.gov.hmcts.reform.professionalapi.controller.response;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;

class OrganisationsWithPbaStatusResponseTest {

    @Test
    void testOrganisationsWithPbaStatusResponse() {
        FetchPbaByStatusResponse pba1 = new FetchPbaByStatusResponse();
        pba1.setStatus(ACCEPTED.name());
        pba1.setPbaNumber("PBA123456");
        pba1.setDateCreated(LocalDateTime.now().toString());
        pba1.setDateAccepted(LocalDateTime.now().toString());
        List<FetchPbaByStatusResponse> pbas = Collections.singletonList(pba1);

        OrganisationsWithPbaStatusResponse organisations =
                new OrganisationsWithPbaStatusResponse("ABCDEFG7", OrganisationStatus.ACTIVE, pbas,
                        "ORGNAME", Collections.emptyList());

        assertEquals("ABCDEFG7", organisations.getOrganisationIdentifier());
        assertEquals(1, organisations.getPbaNumbers().size());
        assertEquals(OrganisationStatus.ACTIVE, organisations.getStatus());
    }

    @Test
    void testOrganisationsWithPbaStatusResponseSetter() {
        PaymentAccount pba1 = new PaymentAccount();
        pba1.setPbaStatus(ACCEPTED);
        pba1.setPbaNumber("PBA123456");
        pba1.setCreated(LocalDateTime.now());
        pba1.setLastUpdated(LocalDateTime.now());
        List<PaymentAccount> pbas = Collections.singletonList(pba1);
        List<FetchPbaByStatusResponse> pbaResponse = new ArrayList<>();

        OrganisationsWithPbaStatusResponse organisations = new OrganisationsWithPbaStatusResponse();
        organisations.setStatus(OrganisationStatus.ACTIVE);
        organisations.setPbaNumbers(pbaResponse);
        organisations.setOrganisationIdentifier("ABCDEFG7");

        assertEquals("ABCDEFG7", organisations.getOrganisationIdentifier());
        assertEquals(0, organisations.getPbaNumbers().size());
        assertEquals(OrganisationStatus.ACTIVE, organisations.getStatus());
    }
}
