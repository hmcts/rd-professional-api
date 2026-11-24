package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.FIND_BY_PBA_STATUS_1;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.FIND_BY_PBA_STATUS_2;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.FIND_BY_PBA_STATUS_3;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.FIND_BY_PBA_STATUS_4;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {

    Organisation findByName(String name);

    Organisation findByOrganisationIdentifier(String id);

    @Query(value = "SELECT * FROM dbrefdata.organisation o WHERE "
            + "((COALESCE(:orgTypes) IS NULL OR o.org_type IN (:orgTypes)) "
            + "OR (:includeV1Orgs = TRUE AND o.org_type IS NULL)) "
            + "AND (COALESCE(:searchAfter) IS NULL OR o.id > :searchAfter) "
            + "ORDER BY o.id ASC", nativeQuery = true)
    Page<Organisation> findByOrgTypeIn(@Param("orgTypes") List<String> orgTypes,
                                       @Param("searchAfter") UUID searchAfter,
                                       @Param("includeV1Orgs") boolean includeV1Orgs,
                                       Pageable pageable);

    Organisation findByCompanyNumber(String companyNumber);

    Organisation findByUsers(ProfessionalUser user);

    List<Organisation> findByStatus(OrganisationStatus status);

    @EntityGraph(value = "Organisation.alljoins")
    Page<Organisation> findByStatus(OrganisationStatus status, Pageable pageable);

    List<Organisation> findByStatusIn(List<OrganisationStatus> statuses);

    @EntityGraph(value = "Organisation.alljoins")
    Page<Organisation> findByStatusIn(List<OrganisationStatus> statuses, Pageable pageable);

    List<Organisation> findByStatusInAndLastUpdatedGreaterThanEqual(List<OrganisationStatus> statuses,
                                                                    LocalDateTime since);

    @EntityGraph(value = "Organisation.alljoins")
    Page<Organisation> findByStatusInAndLastUpdatedGreaterThanEqual(List<OrganisationStatus> statuses,
                                                                    LocalDateTime since, Pageable pageable);

    @EntityGraph(value = "Organisation.alljoins")
    List<Organisation> findAll();

    @EntityGraph(value = "Organisation.alljoins")
    List<Organisation> findByLastUpdatedGreaterThanEqual(LocalDateTime since);

    @Query(FIND_BY_PBA_STATUS_1 + FIND_BY_PBA_STATUS_2 + FIND_BY_PBA_STATUS_3 + FIND_BY_PBA_STATUS_4)
    List<Organisation> findByPbaStatus(@Param("pbaStatus") PbaStatus pbaStatus);
}
