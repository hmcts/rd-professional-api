package uk.gov.hmcts.reform.professionalapi.repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;

@Repository
public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, UUID> {

    List<PaymentAccount> findByPbaNumber(String pbaNumbersFrom);

    List<PaymentAccount> findByPbaNumberIn(Set<String> pbaNumbers);

    @Query(value = "SELECT paymentAccount FROM payment_account pu WHERE pu.pbaStatus = :pbaStatus",
            nativeQuery = true)
    List<PaymentAccount> findByPbaStatus(@Param("pbaStatus") PbaStatus pbaStatus);
}
