package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.OrgAttribute;

import java.util.UUID;

@Repository
public interface OrgAttributeRepository extends JpaRepository<OrgAttribute, UUID> {

    @Modifying
    @Query(value = "delete from dbrefdata.org_attributes org where org.ORGANISATION_ID in (:val)",nativeQuery = true)
    void deleteByOrganistion(UUID val);
}
