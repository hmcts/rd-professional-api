package uk.gov.hmcts.reform.professionalapi.repository;

import java.util.UUID;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;

@Repository
public interface ContactInformationRepository extends JpaRepository<ContactInformation, UUID> {

    @Modifying
    @Query("Delete from contact_information ci where ci.id in (:addressIds)")
    void deleteByAddressId(Set<String> addressIds);
}
