package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.PbaAccountCreationRequest.aPbaPaymentAccount;

import org.junit.Before;
import org.junit.Test;

public class PbaAccountCreationRequestTest {

    private PbaAccountCreationRequest pbaAccountCreationRequest;
    private final String pbaNumber = "123456";

    @Before
    public void setUp() {
        pbaAccountCreationRequest = new PbaAccountCreationRequest(pbaNumber);
    }

    @Test
    public void test_PbaAccountCreation() {
        String expectedPbaNumber = pbaAccountCreationRequest.getPbaNumber();
        assertThat(expectedPbaNumber).isEqualTo(pbaNumber);
    }

    @Test
    public void test_ThatPbaAccountCreationIsChangedByBuilder() {
        System.out.println(pbaNumber);
        String pbaNumber1 = "987654";
        PbaAccountCreationRequest testPbaAccountCreationRequest = aPbaPaymentAccount().pbaNumber(pbaNumber1).build();
        assertThat(testPbaAccountCreationRequest.getPbaNumber()).isEqualTo(pbaNumber1);
    }
}