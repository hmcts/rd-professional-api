package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.OrgAttribute;

import java.util.UUID;

@Repository
public interface OrgAttributeRepository extends JpaRepository<OrgAttribute, UUID> {

    @Modifying
    @Query("DELETE FROM OrgAttribute o WHERE o.organisation.id = :organisationId")
    void deleteByOrganistion(@Param("organisationId") UUID organisationId);
}
