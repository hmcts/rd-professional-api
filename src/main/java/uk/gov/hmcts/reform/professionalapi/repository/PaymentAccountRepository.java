package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PaymentAccountRepository extends JpaRepository<PaymentAccount, UUID> {

    Optional<PaymentAccount> findByPbaNumber(String pbaNumber);

    Optional<PaymentAccount> findByPbaNumberAndOrganisationId(String pbaNumber, UUID organisationId);

    List<PaymentAccount> findByPbaNumberIn(Set<String> pbaNumbers);

    @Modifying
    @Query("DELETE FROM PaymentAccount p WHERE UPPER(p.pbaNumber) = UPPER(:pbaNumber)")
    void deleteByPbaNumberUpperCase(@Param("pbaNumber") String pbaNumber);
}
