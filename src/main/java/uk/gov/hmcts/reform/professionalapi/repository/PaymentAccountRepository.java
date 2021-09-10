package uk.gov.hmcts.reform.professionalapi.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

@Repository
public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, UUID> {

    Optional<PaymentAccount> findByPbaNumber(String pbaNumber);

    List<PaymentAccount> findByPbaNumberIn(Set<String> pbaNumbers);

    void deleteByPbaNumber(String pbaNumber);
}
