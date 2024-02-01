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

    List<Organisation> findByOrgTypeIn(List<String> ids);

    Page<Organisation> findByOrgTypeIn(List<String> ids, Pageable pageable);

    List<Organisation> findByOrgTypeInAndIdGreaterThan(List<String> ids, UUID searchAfter);

    Page<Organisation> findByOrgTypeInAndIdGreaterThan(List<String> ids, UUID searchAfter, Pageable pageable);

    Organisation findByCompanyNumber(String companyNumber);

    Organisation findByUsers(ProfessionalUser user);

    List<Organisation> findByStatus(OrganisationStatus status);

    @EntityGraph(value = "Organisation.alljoins")
    Page<Organisation> findByStatus(OrganisationStatus status, Pageable pageable);

    List<Organisation> findByStatusIn(List<OrganisationStatus> statuses);

    @EntityGraph(value = "Organisation.alljoins")
    Page<Organisation> findByStatusIn(List<OrganisationStatus> statuses, Pageable pageable);

    @EntityGraph(value = "Organisation.alljoins")
    List<Organisation> findAll();

    List<Organisation> findByIdGreaterThan(UUID searchAfter);

    Page<Organisation> findByIdGreaterThan(UUID searchAfter, Pageable pageable);

    @Query(FIND_BY_PBA_STATUS_1 + FIND_BY_PBA_STATUS_2 + FIND_BY_PBA_STATUS_3 + FIND_BY_PBA_STATUS_4)
    List<Organisation> findByPbaStatus(@Param("pbaStatus") PbaStatus pbaStatus);
}
