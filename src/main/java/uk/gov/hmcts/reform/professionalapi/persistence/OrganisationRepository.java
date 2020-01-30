package uk.gov.hmcts.reform.professionalapi.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {

    Organisation findByName(String name);

    @Query(value = "SELECT * FROM Organisation o WHERE o.organisation_identifier = :organisation_identifier", nativeQuery = true)
    Organisation findByOrganisationIdentifier(@Param("organisation_identifier")String id);

    Organisation findByCompanyNumber(String companyNumber);

    Organisation findByUsers(ProfessionalUser user);


    //@Query(value = "SELECT * FROM organisation org INNER JOIN professional_user pu ON org.ORGANISATION_IDENTIFIER::uuid = pu.ORGANISATION_ID::uuid INNER JOIN payment_account pa ON org.ORGANISATION_IDENTIFIER::uuid = pa.ORGANISATION_ID::uuid INNER JOIN contact_information ci ON org.ORGANISATION_IDENTIFIER::uuid = ci.ORGANISATION_ID::uuid WHERE org.status = :status", nativeQuery = true)
    //@Query(value = "SELECT * FROM organisation org JOIN professional_user pu ON org.ORGANISATION_IDENTIFIER = pu.ORGANISATION_ID", nativeQuery = true)
    @Query(value = "SELECT * FROM organisation org WHERE org.status = :status", nativeQuery = true)
    List<Organisation> findByStatus(@Param("status")String status);

}
