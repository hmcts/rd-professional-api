package uk.gov.hmcts.reform.professionalapi.controller.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.professionalapi.controller.request.PbaAccountCreationRequest.aPbaPaymentAccount;

@ExtendWith(MockitoExtension.class)
class PbaAccountCreationRequestTest {

    private PbaAccountCreationRequest pbaAccountCreationRequest;
    private final String pbaNumber = "123456";
    private final String pbaNumber1 = "987654";

    @BeforeEach
    void setUp() {
        pbaAccountCreationRequest = new PbaAccountCreationRequest(pbaNumber);
    }

    @Test
    void test_PbaAccountCreation() {
        String expectedPbaNumber = pbaAccountCreationRequest.getPbaNumber();
        assertThat(expectedPbaNumber).isEqualTo(pbaNumber);
    }

    @Test
    void test_ThatPbaAccountCreationIsChangedByBuilder() {
        System.out.println(pbaNumber);
        PbaAccountCreationRequest testPbaAccountCreationRequest = aPbaPaymentAccount().pbaNumber(pbaNumber1).build();
        assertThat(testPbaAccountCreationRequest.getPbaNumber()).isEqualTo(pbaNumber1);
    }
}