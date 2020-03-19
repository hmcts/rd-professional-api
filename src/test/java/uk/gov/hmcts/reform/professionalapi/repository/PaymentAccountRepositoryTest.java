package uk.gov.hmcts.reform.professionalapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.helper.RepositorySetUp;

@RunWith(SpringRunner.class)
@DataJpaTest
public class PaymentAccountRepositoryTest extends RepositorySetUp {

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

    @Test
    public void test_findByPbaNumber() {
        List<PaymentAccount> paymentAccounts = paymentAccountRepository.findByPbaNumber(paymentAccount.getPbaNumber());

        assertThat(paymentAccounts).hasSize(1);
        assertThat(paymentAccounts.get(0)).isEqualTo(paymentAccount);
    }

    @Test
    public void test_findByPbaNumberIn() {
        Set<String> pbaNumbers = new HashSet<>();
        pbaNumbers.add(paymentAccount.getPbaNumber());

        List<PaymentAccount> paymentAccounts = paymentAccountRepository.findByPbaNumberIn(pbaNumbers);

        assertThat(paymentAccounts).hasSize(1);
        assertThat(paymentAccounts.get(0)).isEqualTo(paymentAccount);
    }
}
