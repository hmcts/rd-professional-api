package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import static java.util.Collections.singleton;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;

public class PaymentAccountValidatorTest {

    @Mock
    private PaymentAccountRepository paymentAccountRepository = mock(PaymentAccountRepository.class);

    PaymentAccountValidator paymentAccountValidator = new PaymentAccountValidator(paymentAccountRepository);

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test(expected = Test.None.class)
    public void testPbaNumberIsValid() {
        Set<String> pbas = new HashSet<>();
        pbas.add("PBA1234567");
        pbas.add("pba1234567");
        pbas.add("PbA1234567");
        PaymentAccountValidator.checkPbaNumberIsValid(pbas);
    }

    @Test
    public void testPbaNumberIsInValid() {
        Set<String> pbaNumber = new HashSet<>();
        pbaNumber.add("abc1234567");

        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(pbaNumber))
                .isExactlyInstanceOf(InvalidRequest.class);

        pbaNumber.clear();
        pbaNumber.add("pba123456");

        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(pbaNumber))
                .isExactlyInstanceOf(InvalidRequest.class);

        pbaNumber.clear();
        pbaNumber.add("1234");

        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(pbaNumber))
                .isExactlyInstanceOf(InvalidRequest.class);

        pbaNumber.clear();
        pbaNumber.add("wewdfd");

        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(pbaNumber))
                .isExactlyInstanceOf(InvalidRequest.class);
    }

    @Test(expected = Test.None.class)
    public void test_CheckPbasAreUniqueWithOrgId() {
        paymentAccountValidator.checkPbasAreUniqueWithOrgId(singleton("PBA1234567"), "");
    }

    @Test
    public void test_ValidatePaymentAccounts() {
        String pba = "PBA1234567";
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add(pba);
        paymentAccountValidator.validatePaymentAccounts(paymentAccounts, "");
    }

    @Test
    public void test_checkSinglePbaIsValid_True() {
        Assert.assertTrue(paymentAccountValidator.checkSinglePbaIsValid("PBA1234567"));
    }

    @Test
    public void test_checkSinglePbaIsValid_False() {
        Assert.assertFalse(paymentAccountValidator.checkSinglePbaIsValid("abc1234567"));
    }

    @Test(expected = Test.None.class)
    public void test_checkSinglePbaIsUnique() {
        paymentAccountValidator.checkSinglePbaIsUnique("PBA1234567");
    }
}