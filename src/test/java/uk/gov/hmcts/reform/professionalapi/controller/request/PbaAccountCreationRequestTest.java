package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.PbaAccountCreationRequest.aPbaPaymentAccount;

import org.junit.Before;
import org.junit.Test;

public class PbaAccountCreationRequestTest {

    private PbaAccountCreationRequest pbaAccountCreationRequest;
    private String pbaNumber = "123456";
    private String pbaNumber1 = "987654";

    @Before
    public void setUp() {
        pbaAccountCreationRequest = new PbaAccountCreationRequest(pbaNumber);
    }

    @Test
    public void testPbaAccountCreation() {
        String expectedPbaNumber = pbaAccountCreationRequest.getPbaNumber();
        assertThat(expectedPbaNumber).isEqualTo(pbaNumber);
    }

    @Test
    public void testThatPbaAccountCreationIsChangedByBuilder() {
        System.out.println(pbaNumber);
        PbaAccountCreationRequest testPbaAccountCreationRequest = aPbaPaymentAccount().pbaNumber(pbaNumber1).build();
        assertThat(testPbaAccountCreationRequest.getPbaNumber()).isEqualTo(pbaNumber1);
    }
}