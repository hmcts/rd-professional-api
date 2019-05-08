package uk.gov.hmcts.reform.professionalapi.service.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, UUID> {

    List<PaymentAccount> findByPbaNumber(String pbaNumbersFrom);
}
