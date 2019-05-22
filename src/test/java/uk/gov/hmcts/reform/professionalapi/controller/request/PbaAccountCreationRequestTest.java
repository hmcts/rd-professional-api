package uk.gov.hmcts.reform.professionalapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.PbaAccountCreationRequest.aPbaPaymentAccount;

import org.junit.Before;
import org.junit.Test;

public class PbaAccountCreationRequestTest {

    private PbaAccountCreationRequest pbaAccountCreationRequest;

    private String pbaNumber = "123456";

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
        String pbaNumber2 = "987654";
        PbaAccountCreationRequest testPbaAccountCreationRequest = aPbaPaymentAccount().pbaNumber(pbaNumber2).build();
        assertThat(testPbaAccountCreationRequest.getPbaNumber()).isEqualTo(pbaNumber2);
    }


}