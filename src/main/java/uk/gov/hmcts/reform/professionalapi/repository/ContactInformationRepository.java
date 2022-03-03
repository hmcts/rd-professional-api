package uk.gov.hmcts.reform.professionalapi.repository;

import java.util.UUID;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;

@Repository
public interface ContactInformationRepository extends JpaRepository<ContactInformation, UUID> {

    void deleteByIdIn(Set<UUID> idsSet);
}
