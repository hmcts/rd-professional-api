package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;

import org.assertj.core.api.Assertions;
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
        MockitoAnnotations.initMocks(this);
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
        final Set<String> pbaNumber = singleton("abc1234567");
        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(pbaNumber))
                .isExactlyInstanceOf(InvalidRequest.class);

        final Set<String> pbaNumber1 = singleton("abc1234567");
        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(pbaNumber1))
                .isExactlyInstanceOf(InvalidRequest.class);

        final Set<String> pbaNumber2 = singleton("1234");
        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(pbaNumber2))
                .isExactlyInstanceOf(InvalidRequest.class);

        final Set<String> pbaNumber3 = singleton("wewdfd");
        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(pbaNumber3))
                .isExactlyInstanceOf(InvalidRequest.class);

        final Set<String> pbaNumber4 = singleton(null);
        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(pbaNumber4))
                .isExactlyInstanceOf(InvalidRequest.class);
    }

    @Test(expected = Test.None.class)
    public void testCheckPbasAreUniqueWithOrgId() {
        paymentAccountValidator.checkPbasAreUniqueWithOrgId(singleton("PBA1234567"), "");
    }

    @Test
    public void testValidatePaymentAccounts() {
        String pba = "PBA1234567";
        Set<String> paymentAccounts = new HashSet<>();
        paymentAccounts.add(pba);
        paymentAccountValidator.validatePaymentAccounts(paymentAccounts, "");
    }


    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<PaymentAccountValidator> constructor = PaymentAccountValidator.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}