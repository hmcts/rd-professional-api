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

    @EntityGraph(value = "Organisation.alljoins")
    @Query(value = """
            SELECT o FROM Organisation o
            WHERE o.status IN :statuses
              AND (:since IS NULL OR o.lastUpdated >= :since)
              AND (
                    LOWER(o.name) LIKE LOWER(CONCAT('%', :searchFilter, '%'))
                 OR LOWER(o.sraId) LIKE LOWER(CONCAT('%', :searchFilter, '%'))
                 OR EXISTS (
                        SELECT ci FROM contact_information ci
                        WHERE ci.organisation = o
                          AND LOWER(REPLACE(ci.postCode, ' ', ''))
                              LIKE LOWER(CONCAT('%', REPLACE(:searchFilter, ' ', ''), '%'))
                    )
                 OR EXISTS (
                        SELECT pa FROM payment_account pa
                        WHERE pa.organisation = o
                          AND LOWER(pa.pbaNumber) LIKE LOWER(CONCAT('%', :searchFilter, '%'))
                    )
                 OR EXISTS (
                        SELECT dx FROM dx_address dx
                        WHERE dx.contactInformation.organisation = o
                          AND (
                                LOWER(dx.dxNumber) LIKE LOWER(CONCAT('%', :searchFilter, '%'))
                             OR LOWER(dx.dxExchange) LIKE LOWER(CONCAT('%', :searchFilter, '%'))
                          )
                    )
                 OR EXISTS (
                        SELECT su FROM super_user_view su
                        WHERE su.organisation = o
                          AND LOWER(CONCAT(CONCAT(su.firstName, ' '), su.lastName))
                              LIKE LOWER(CONCAT('%', :searchFilter, '%'))
                    )
              )
            """)
    Page<Organisation> findByStatusInAndSearchFilter(
            @Param("statuses") List<OrganisationStatus> statuses,
            @Param("since") LocalDateTime since,
            @Param("searchFilter") String searchFilter,
            Pageable pageable);

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
