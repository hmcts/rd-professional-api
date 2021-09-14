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

    @Query(value = "select * from organisation as o, payment_account as p \n"
            + "where p.organisation_id = o.id \n"
            + "and p.pba_status = :pbaStatus \n"
            + "order by p.created asc", nativeQuery = true)
    List<Organisation> findByPbaStatus(@Param("pbaStatus")String pbaStatus);
}
