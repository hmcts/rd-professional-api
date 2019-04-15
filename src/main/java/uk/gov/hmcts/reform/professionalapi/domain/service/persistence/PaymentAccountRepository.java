package uk.gov.hmcts.reform.professionalapi.domain.service.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.reform.professionalapi.domain.entities.PaymentAccount;

public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, UUID> {

    List<PaymentAccount> findByPbaNumber(String pbaNumbersFrom);
}
