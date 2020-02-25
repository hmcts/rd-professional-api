package uk.gov.hmcts.reform.professionalapi.controller.request;

import static java.util.Collections.singleton;

import java.util.HashSet;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class PaymentAccountValidatorTest {

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
        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(singleton("abc1234567")))
                .isExactlyInstanceOf(InvalidRequest.class);

        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(singleton("pba123456")))
                .isExactlyInstanceOf(InvalidRequest.class);

        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(singleton("1234")))
                .isExactlyInstanceOf(InvalidRequest.class);

        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(singleton("wewdfd")))
                .isExactlyInstanceOf(InvalidRequest.class);

        Assertions.assertThatThrownBy(() -> PaymentAccountValidator.checkPbaNumberIsValid(singleton(null)))
                .isExactlyInstanceOf(InvalidRequest.class);
    }
}