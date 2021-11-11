package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;

@ExtendWith(MockitoExtension.class)
class PaymentAccountValidatorTest {

    @Mock
    private PaymentAccountRepository paymentAccountRepository = mock(PaymentAccountRepository.class);

    PaymentAccountValidator paymentAccountValidator = new PaymentAccountValidator(paymentAccountRepository);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testPbaNumberIsValid() {
        Set<String> pbas = new HashSet<>();
        pbas.add("PBA1234567");
        pbas.add("pba1234567");
        pbas.add("PbA1234567");
        assertDoesNotThrow(() ->
                PaymentAccountValidator.checkPbaNumberIsValid(pbas));
    }

    @Test
    void testPbaNumberIsInValid() {
        Set<String> pbaNumber = new HashSet<>();
        pbaNumber.add("abc1234567");

        assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(pbaNumber))
                .isExactlyInstanceOf(InvalidRequest.class);

        pbaNumber.clear();
        pbaNumber.add("pba123456");

        assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(pbaNumber))
                .isExactlyInstanceOf(InvalidRequest.class);

        pbaNumber.clear();
        pbaNumber.add("1234");

        assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(pbaNumber))
                .isExactlyInstanceOf(InvalidRequest.class);

        pbaNumber.clear();
        pbaNumber.add("wewdfd");

        assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(pbaNumber))
                .isExactlyInstanceOf(InvalidRequest.class);
    }

    @Test
    void test_CheckPbasAreUniqueWithOrgId() {
        assertDoesNotThrow(() ->
                paymentAccountValidator.checkPbasAreUniqueWithOrgId(singleton("PBA1234567"), ""));
    }

    @Test
    void test_ValidatePaymentAccounts() {
        String pba = "PBA1234567";
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add(pba);
        paymentAccountValidator.validatePaymentAccounts(paymentAccounts, "");
    }
}