package uk.gov.hmcts.reform.professionalapi.persistence;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

@Repository
public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, UUID> {

    void deleteByPbaNumberIn(List<String> pbaNumber);

    List<PaymentAccount> findByPbaNumber(String pbaNumbersFrom);

    void deleteByIdIn(List<UUID> accountIds);

    List<PaymentAccount> findByPbaNumberIn(Set<String> pbaNumbers);
}
