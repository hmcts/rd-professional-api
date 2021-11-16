package uk.gov.hmcts.reform.professionalapi.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.helper.BaseRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
class PaymentAccountRepositoryTest extends BaseRepository {

    @Test
    void test_findAll() {
        List<PaymentAccount> paymentAccounts = paymentAccountRepository.findAll();

        assertThat(paymentAccounts).hasSize(1);
        assertThat(paymentAccounts.get(0)).isEqualTo(paymentAccount);
        assertThat(paymentAccounts.get(0).getId()).isEqualTo(paymentAccount.getId());
    }

    @Test
    void findByIdTest() {
        Optional<PaymentAccount> paymentAcc = paymentAccountRepository.findById(paymentAccount.getId());

        assertThat(paymentAcc).contains(paymentAccount);
        assertThat(paymentAcc.get().getId()).isEqualTo(paymentAccount.getId());
    }

    @Test
    void test_findByPbaNumber() {
        List<PaymentAccount> paymentAccounts = paymentAccountRepository.findByPbaNumber(paymentAccount.getPbaNumber());

        assertThat(paymentAccounts).hasSize(1);
        assertThat(paymentAccounts.get(0)).isEqualTo(paymentAccount);
        assertThat(paymentAccounts.get(0).getId()).isEqualTo(paymentAccount.getId());
    }

    @Test
    void test_findByPbaNumberIn() {
        Set<String> pbaNumbers = new HashSet<>();
        pbaNumbers.add(paymentAccount.getPbaNumber());

        List<PaymentAccount> paymentAccounts = paymentAccountRepository.findByPbaNumberIn(pbaNumbers);

        assertThat(paymentAccounts).hasSize(1);
        assertThat(paymentAccounts.get(0)).isEqualTo(paymentAccount);
        assertThat(paymentAccounts.get(0).getId()).isEqualTo(paymentAccount.getId());
    }
}
