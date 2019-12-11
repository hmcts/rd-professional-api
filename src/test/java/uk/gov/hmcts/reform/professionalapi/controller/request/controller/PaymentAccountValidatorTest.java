package uk.gov.hmcts.reform.professionalapi.controller.request.controller;

import java.util.HashSet;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PaymentAccountValidator;


public class PaymentAccountValidatorTest {

    private final PaymentAccountValidator sut = new PaymentAccountValidator();

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

        final Set<String> pbas = new HashSet<>();
        pbas.add("abc1234567");
        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(pbas))
                .isExactlyInstanceOf(InvalidRequest.class);

        final Set<String> pbas1 = new HashSet<>();
        pbas1.add("pba123456");
        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(pbas1))
                .isExactlyInstanceOf(InvalidRequest.class);

        final Set<String> pbas2 = new HashSet<>();
        pbas2.add("1234");
        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(pbas2))
                .isExactlyInstanceOf(InvalidRequest.class);

        final Set<String> pbas3 = new HashSet<>();
        pbas3.add("wewdfd");
        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(pbas3))
                .isExactlyInstanceOf(InvalidRequest.class);

        final Set<String> pbas5 = new HashSet<>();
        pbas5.add(null);
        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(pbas5))
                .isExactlyInstanceOf(InvalidRequest.class);

    }
}