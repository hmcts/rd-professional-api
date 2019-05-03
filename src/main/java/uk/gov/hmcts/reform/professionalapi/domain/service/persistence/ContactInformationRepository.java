package uk.gov.hmcts.reform.professionalapi.domain.service.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import uk.gov.hmcts.reform.professionalapi.domain.entities.ContactInformation;

public interface ContactInformationRepository extends JpaRepository<ContactInformation, UUID> {

}
