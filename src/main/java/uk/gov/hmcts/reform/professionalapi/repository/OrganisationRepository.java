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

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, UUID> {

    Organisation findByName(String name);

    Organisation findByOrganisationIdentifier(String id);

    Organisation findByCompanyNumber(String companyNumber);

    Organisation findByUsers(ProfessionalUser user);

    List<Organisation> findByStatus(OrganisationStatus status);

    List<Organisation> findByStatusIn(List<OrganisationStatus> statuses);

    List<Organisation> findByStatusInAndLastUpdatedGreaterThanEqual(List<OrganisationStatus> statuses,
                                                                    LocalDateTime since);

    // Fetch organisations by statuses with eager joins
    @EntityGraph(value = "Organisation.alljoins")
    Page<Organisation> findByStatus(OrganisationStatus status, Pageable pageable);

    @EntityGraph(value = "Organisation.alljoins")
    Page<Organisation> findByStatusIn(List<OrganisationStatus> statuses, Pageable pageable);

    @EntityGraph(value = "Organisation.alljoins")
    Page<Organisation> findByStatusInAndLastUpdatedGreaterThanEqual(List<OrganisationStatus> statuses,
                                                                    LocalDateTime since, Pageable pageable);

    @EntityGraph(value = "Organisation.alljoins")
    List<Organisation> findAll();

    @EntityGraph(value = "Organisation.alljoins")
    List<Organisation> findByLastUpdatedGreaterThanEqual(LocalDateTime since);

    // Fetch organisations filtered by orgType(s) and optionally legacy V1 Orgs
    @EntityGraph(value = "Organisation.alljoins")
    @Query("SELECT o FROM Organisation o " +
           "WHERE (:orgTypes IS NULL OR o.orgType IN :orgTypes) " +
           "OR (:includeV1Orgs = TRUE AND o.orgType IS NULL) " +
           "AND (:searchAfter IS NULL OR o.id > :searchAfter) " +
           "ORDER BY o.id ASC")
    Page<Organisation> findByOrgTypeIn(@Param("orgTypes") List<String> orgTypes,
                                       @Param("searchAfter") UUID searchAfter,
                                       @Param("includeV1Orgs") boolean includeV1Orgs,
                                       Pageable pageable);

    // Custom query for PBA status filtering using JPQL
    @EntityGraph(value = "Organisation.alljoins")
    @Query("SELECT DISTINCT o FROM Organisation o " +
           "JOIN o.paymentAccounts pba " +
           "WHERE pba.pbaStatus = :pbaStatus")
    List<Organisation> findByPbaStatus(@Param("pbaStatus") PbaStatus pbaStatus);
}
