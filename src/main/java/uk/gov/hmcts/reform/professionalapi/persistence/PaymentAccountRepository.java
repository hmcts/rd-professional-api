package uk.gov.hmcts.reform.professionalapi.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

@Repository
public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, UUID> {

    List<PaymentAccount> findByPbaNumber(String pbaNumbersFrom);
}
