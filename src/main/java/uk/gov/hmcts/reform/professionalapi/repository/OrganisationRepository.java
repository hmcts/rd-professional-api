package uk.gov.hmcts.reform.professionalapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
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
public interface OrganisationRepository extends PagingAndSortingRepository<Organisation, UUID> {

    Organisation findByName(String name);

    Organisation findByOrganisationIdentifier(String id);

    Organisation findByCompanyNumber(String companyNumber);

    Organisation findByUsers(ProfessionalUser user);

    List<Organisation> findByStatus(OrganisationStatus status);

    Page<Organisation> findByStatus(OrganisationStatus status, Pageable pageable);

    List<Organisation> findByStatusIn(List<OrganisationStatus> statuses);

    Page<Organisation> findByStatusIn(List<OrganisationStatus> statuses, Pageable pageable);

    @EntityGraph(value = "Organisation.alljoins")
    List<Organisation> findAll();

    @EntityGraph(value = "Organisation.alljoins")
    Page<Organisation> findAll(Pageable pageable);

    @Query(FIND_BY_PBA_STATUS_1 + FIND_BY_PBA_STATUS_2 + FIND_BY_PBA_STATUS_3 + FIND_BY_PBA_STATUS_4)
    List<Organisation> findByPbaStatus(@Param("pbaStatus") PbaStatus pbaStatus);
}
