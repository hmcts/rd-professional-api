package uk.gov.hmcts.reform.professionalapi.service;

import java.util.List;
import java.util.UUID;

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;


public interface OrganisationService {

    OrganisationResponse createOrganisationFrom(OrganisationCreationRequest organisationCreationRequest);

    OrganisationsDetailResponse retrieveOrganisations();

    OrganisationEntityResponse retrieveOrganisation(UUID organisationId);

    OrganisationResponse updateOrganisation(OrganisationCreationRequest organisationCreationRequest, UUID organisationIdentifier);

    Organisation getOrganisationByOrganisationIdentifier(UUID organisationIdentifier);

    public List<ProfessionalUser> findProfessionalUsersByOrganisation(UUID organisationIdentifier, boolean showDeleted);

}

