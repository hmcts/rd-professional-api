package uk.gov.hmcts.reform.professionalapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

@RunWith(SpringRunner.class)
@DataJpaTest
@Ignore
public class PaymentAccountRepositoryTest {

    @Autowired
    PaymentAccountRepository paymentAccountRepository;

    PaymentAccount paymentAccount = new PaymentAccount("pba1234567");

    @Before
    public void setUp() {
        paymentAccountRepository.save(paymentAccount);
    }

    @Test
    public void test_findAll() {
        List<PaymentAccount> paymentAccounts = paymentAccountRepository.findAll();

        assertThat(paymentAccounts).hasSize(1);
        assertThat(paymentAccounts.get(0)).isEqualTo(paymentAccount);
    }

    @Test
    public void findByIdTest() {
        Optional<PaymentAccount> paymentAcc = paymentAccountRepository.findById(paymentAccount.getId());

        assertThat(paymentAcc.get()).isEqualTo(paymentAccount);
    }
}
