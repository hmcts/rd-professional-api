package uk.gov.hmcts.reform.professionalapi.repository;

import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ProfessionalUserRepository extends JpaRepository<ProfessionalUser, UUID> {

    ProfessionalUser findByEmailAddress(String email);

    List<ProfessionalUser> findByOrganisationAndDeletedNotNull(Organisation organisation);

    Page<ProfessionalUser> findByOrganisation(Organisation organisation, Pageable pageable);

    List<ProfessionalUser> findByOrganisation(Organisation organisation);

    List<ProfessionalUser> findByOrganisationAndUserIdentifier(Organisation organisation, String userIdentifier);

    ProfessionalUser findByUserIdentifier(String userIdentifier);

    @Query(value = "SELECT count(*) FROM dbrefdata.professional_user pu WHERE pu.organisation_id = :organisationId",
            nativeQuery = true)
    int findByUserCountByOrganisationId(@Param("organisationId") UUID organisationId);

    @Query(value = """
        SELECT pu.* FROM dbrefdata.professional_user pu
        INNER JOIN dbrefdata.organisation organisation ON pu.organisation_id = organisation.Id
        WHERE (COALESCE(:organisationIdentifiers, NULL) is NULL
        OR (organisation.organisation_identifier in (:organisationIdentifiers))
        AND pu.user_identifier IS NOT NULL
        AND TRIM(pu.user_identifier) <> '')ORDER BY pu.organisation_id, pu.id

        """,countQuery = """
    SELECT COUNT(*) 
    FROM dbrefdata.professional_user pu
    INNER JOIN dbrefdata.organisation organisation
      ON pu.organisation_id = organisation.id
    WHERE 
      ( :organisationIdentifiers IS NULL 
        OR organisation.organisation_identifier IN (:organisationIdentifiers)
      )
      AND pu.user_identifier IS NOT NULL
      AND TRIM(pu.user_identifier) <> ''
        """, nativeQuery = true)
    Page<ProfessionalUser> findUsersInOrganisations(
            @Param("organisationIdentifiers") List<String> organisationIdentifiers, Pageable pageable);


    @Query(
        value = """
        SELECT pu.* FROM dbrefdata.professional_user pu
        INNER JOIN dbrefdata.organisation organisation ON pu.organisation_id = organisation.Id
        WHERE (COALESCE(:organisationIdentifiers, NULL) IS NULL
        OR organisation.organisation_identifier IN (:organisationIdentifiers))
        AND ((organisation.Id = :searchAfterOrgId AND pu.Id > :searchAfterUserId)
        OR (organisation.Id > :searchAfterOrgId))
        AND pu.user_identifier IS NOT NULL
        AND TRIM(pu.user_identifier) <> ''
        ORDER BY pu.organisation_id, pu.id
        """,
        countQuery = """
        SELECT COUNT(*) FROM dbrefdata.professional_user pu
        INNER JOIN dbrefdata.organisation organisation ON pu.organisation_id = organisation.Id
        WHERE (COALESCE(:organisationIdentifiers, NULL) IS NULL
        OR organisation.organisation_identifier IN (:organisationIdentifiers))
        AND ((organisation.Id = :searchAfterOrgId AND pu.Id > :searchAfterUserId)
        OR (organisation.Id > :searchAfterOrgId))
        AND pu.user_identifier IS NOT NULL
        AND TRIM(pu.user_identifier) <> ''
        """,
        nativeQuery = true
    )
    Page<ProfessionalUser> findUsersInOrganisationsSearchAfter(
        @Param("organisationIdentifiers") List<String> organisationIdentifiers,
        @Param("searchAfterOrgId") UUID searchAfterOrgId,
        @Param("searchAfterUserId") UUID searchAfterUserId,
        Pageable pageable);


    List<ProfessionalUser> findByLastUpdatedGreaterThanEqual(LocalDateTime lastUpdated);


    @Query(
        value = """
        SELECT pu.* FROM dbrefdata.professional_user pu
        WHERE pu.last_updated >= :lastUpdated
        AND pu.user_identifier IS NOT NULL
        AND TRIM(pu.user_identifier) <> ''
        ORDER BY pu.id ASC
        """,
        countQuery = """
        SELECT COUNT(*) FROM dbrefdata.professional_user pu
        WHERE pu.last_updated >= :lastUpdated
        AND pu.user_identifier IS NOT NULL
        AND TRIM(pu.user_identifier) <> ''
        """, nativeQuery = true
    )
    Page<ProfessionalUser> findByLastUpdatedGreaterThanEqualAndUserIdentifierIsNotEmpty(
        @Param("lastUpdated") LocalDateTime lastUpdated,
        Pageable pageable);

    List<ProfessionalUser> findByLastUpdatedGreaterThanEqualAndIdGreaterThan(LocalDateTime lastUpdated,
                                                                                  UUID searchAfter);



    @Query(
        value = """
        SELECT pu.* FROM dbrefdata.professional_user pu
        WHERE pu.last_updated >= :lastUpdated
          AND pu.id > :searchAfter
          AND pu.user_identifier IS NOT NULL
          AND pu.user_identifier <> ''
        ORDER BY pu.id ASC
        """,
        countQuery = """
        SELECT COUNT(*) FROM dbrefdata.professional_user pu
        WHERE pu.last_updated >= :lastUpdated
          AND pu.id > :searchAfter
          AND pu.user_identifier IS NOT NULL
          AND pu.user_identifier <> ''
        """, nativeQuery = true
    )
    Page<ProfessionalUser> findByLastUpdatedGreaterThanEqualAndIdGreaterThanAndUserIdentifierIsNotEmpty(
        @Param("lastUpdated") LocalDateTime lastUpdated,
        @Param("searchAfter") UUID searchAfter,
        Pageable pageable);
}
