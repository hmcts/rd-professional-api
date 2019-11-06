package uk.gov.hmcts.reform.professionalapi.service;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

public interface OrganisationService {

    OrganisationResponse createOrganisationFrom(OrganisationCreationRequest organisationCreationRequest);

    OrganisationsDetailResponse retrieveOrganisations();

    ResponseEntity retrieveOrganisationsWithPageable(Pageable pageable);

    OrganisationEntityResponse retrieveOrganisation(String organisationIdentifier);

    OrganisationResponse updateOrganisation(OrganisationCreationRequest organisationCreationRequest, String organisationIdentifier);

    Organisation getOrganisationByOrgIdentifier(String organisationIdentifier);

    OrganisationsDetailResponse findByOrganisationStatus(OrganisationStatus status);

    ResponseEntity findByOrganisationStatusWithPageable(OrganisationStatus status, Pageable pageable);

}

