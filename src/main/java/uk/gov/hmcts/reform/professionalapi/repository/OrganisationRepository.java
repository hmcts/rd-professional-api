package uk.gov.hmcts.reform.professionalapi.repository;

import java.util.List;
import java.util.UUID;


import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {

    Organisation findByName(String name);

    Organisation findByOrganisationIdentifier(String id);

    Organisation findByCompanyNumber(String companyNumber);

    Organisation findByUsers(ProfessionalUser user);

    List<Organisation> findByStatus(OrganisationStatus status);

    List<Organisation> findByStatusIn(List<OrganisationStatus> statuses);

    @EntityGraph(value = "Organisation.alljoins")
    List<Organisation> findAll();

    @SuppressWarnings("java:S6126") //Supressing as Checkstyle breaks when using TextBlocks
    @Query("select o from Organisation o join fetch payment_account p \n"
            + "on p.organisationId = o.id \n"
            + "where p.pbaStatus = :pbaStatus \n"
            + "order by p.created asc")
    List<Organisation> findByPbaStatus(@Param("pbaStatus") PbaStatus pbaStatus);
}
