package uk.gov.hmcts.reform.professionalapi.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;

public interface ContactInformationRepository extends JpaRepository<ContactInformation, UUID> {

}
