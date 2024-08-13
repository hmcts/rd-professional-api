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

    List<ProfessionalUser> findByOrganisationAndUserIdentifier(Organisation organisation, UUID userIdentifier);

    ProfessionalUser findByUserIdentifier(UUID userIdentifier);

    @Query(value = "SELECT count(*) FROM dbrefdata.professional_user pu WHERE pu.organisation_id = :organisationId",
            nativeQuery = true)
    int findByUserCountByOrganisationId(@Param("organisationId") UUID organisationId);

    @Query(value = """
            SELECT pu FROM professional_user pu
            WHERE (COALESCE(:organisationIdentifiers) is NULL
            OR pu.organisation.organisationIdentifier IN :organisationIdentifiers)
            ORDER BY pu.organisation.id ,pu.id
            """)
    Page<ProfessionalUser> findUsersInOrganisations(
            @Param("organisationIdentifiers") List<String> organisationIdentifiers, Pageable pageable);

    @Query(value = """
       SELECT pu.* FROM dbrefdata.professional_user pu
       INNER JOIN dbrefdata.organisation organisation ON pu.organisation_id = organisation.Id
       WHERE (COALESCE(:organisationIdentifiers) is NULL
       OR organisation.organisation_identifier in :organisationIdentifiers)
       AND (
           (organisation.Id::text = :searchAfterOrgId AND pu.Id::text > :searchAfterUserId)
           OR (organisation.Id::text > :searchAfterOrgId)
       )
       ORDER BY pu.organisation_id, pu.id
        """, nativeQuery = true)
    Page<ProfessionalUser> findUsersInOrganisationsSearchAfter(
            @Param("organisationIdentifiers") List<String> organisationIdentifiers,
            @Param("searchAfterOrgId") String searchAfterOrgId, @Param("searchAfterUserId") String searchAfterUserId,
            Pageable pageable);

    List<ProfessionalUser> findByLastUpdatedGreaterThanEqual(LocalDateTime lastUpdated);

    Page<ProfessionalUser> findByLastUpdatedGreaterThanEqual(LocalDateTime lastUpdated, Pageable pageable);

    List<ProfessionalUser> findByLastUpdatedGreaterThanEqualAndIdGreaterThan(LocalDateTime lastUpdated,
                                                                                  UUID searchAfter);

    Page<ProfessionalUser> findByLastUpdatedGreaterThanEqualAndIdGreaterThan(LocalDateTime lastUpdated,
                                                                                  UUID searchAfter,
                                                                                  Pageable pageable);

}
