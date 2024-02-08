package uk.gov.hmcts.reform.professionalapi.repository;

import feign.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @Query("SELECT pu FROM professional_user pu WHERE pu.organisation.organisationIdentifier IN :organisationIdentifiers AND pu.deleted IS NULL")
    List<ProfessionalUser> findByOrganisationIdentifierInAndDeletedNotNull(@Param("organisationIdentifiers") List<String> organisationIdentifiers);

    @Query("SELECT pu FROM professional_user pu WHERE pu.organisation.organisationIdentifier IN :organisationIdentifiers")
    List<ProfessionalUser> findByOrganisationIdentifierIn(@Param("organisationIdentifiers") List<String> organisationIdentifiers);

    @Query("SELECT pu FROM professional_user pu WHERE pu.organisation.organisationIdentifier IN :organisationIdentifiers")
    Page<ProfessionalUser> findByOrganisationIdentifierIn(@Param("organisationIdentifiers") List<String> organisationIdentifiers, Pageable pageable);

    @Query("SELECT pu FROM professional_user pu WHERE pu.deleted IS NULL")
    Page<ProfessionalUser> findAllAndDeletedIsNull(Pageable pageable);

    @Query("SELECT pu FROM professional_user pu WHERE pu.organisation.organisationIdentifier IN " +
            ":organisationIdentifiers and pu.deleted IS NULL")
    Page<ProfessionalUser> findByOrganisationIdentifierInAndDeletedIsNull(@Param("organisationIdentifiers") List<String> organisationIdentifiers, Pageable pageable);

    List<ProfessionalUser> findByLastUpdatedGreaterThanEqual(LocalDateTime lastUpdated);

    Page<ProfessionalUser> findByLastUpdatedGreaterThanEqual(LocalDateTime lastUpdated, Pageable pageable);

    List<ProfessionalUser> findByLastUpdatedGreaterThanEqualAndIdGreaterThan(LocalDateTime lastUpdated,
                                                                                  UUID searchAfter);

    Page<ProfessionalUser> findByLastUpdatedGreaterThanEqualAndIdGreaterThan(LocalDateTime lastUpdated,
                                                                                  UUID searchAfter,
                                                                                  Pageable pageable);
}
