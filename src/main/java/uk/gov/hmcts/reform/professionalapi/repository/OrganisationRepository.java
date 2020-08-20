package uk.gov.hmcts.reform.professionalapi.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {

    Organisation findByName(String name);

    Organisation findByOrganisationIdentifier(String id);

    Organisation findByCompanyNumber(String companyNumber);

    Organisation findByUsers(ProfessionalUser user);

    List<Organisation> findByStatus(OrganisationStatus status);

    @EntityGraph(value = "Organisation.alljoins")
    List<Organisation> findAll();
}
