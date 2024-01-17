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

    List<ProfessionalUser> findByLastUpdatedGreaterThanEqual(LocalDateTime lastUpdated);

    Page<ProfessionalUser> findByLastUpdatedGreaterThanEqual(LocalDateTime lastUpdated, Pageable pageable);

    List<ProfessionalUser> findByLastUpdatedGreaterThanEqualAndCreatedGreaterThan(LocalDateTime lastUpdated,
                                                                                  LocalDateTime searchAfter);

    Page<ProfessionalUser> findByLastUpdatedGreaterThanEqualAndCreatedGreaterThan(LocalDateTime lastUpdated,
                                                                                  LocalDateTime searchAfter,
                                                                                  Pageable pageable);
}
