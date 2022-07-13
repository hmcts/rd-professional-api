package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;

import java.util.Set;
import java.util.UUID;

@Repository
public interface ContactInformationRepository extends JpaRepository<ContactInformation, UUID> {

    void deleteByIdIn(Set<UUID> idsSet);
}
