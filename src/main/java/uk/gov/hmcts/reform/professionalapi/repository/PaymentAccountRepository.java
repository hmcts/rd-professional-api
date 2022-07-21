package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, UUID> {

    Optional<PaymentAccount> findByPbaNumber(String pbaNumber);

    List<PaymentAccount> findByPbaNumberIn(Set<String> pbaNumbers);

    @Modifying
    @Query("Delete from payment_account pba where upper(pba.pbaNumber) in (:paymentAccountsUpper)")
    void deleteByPbaNumberUpperCase(Set<String> paymentAccountsUpper);
}
