package uk.gov.hmcts.reform.professionalapi.service;

import java.util.UUID;

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;


public interface OrganisationService {

    OrganisationResponse createOrganisationFrom(OrganisationCreationRequest organisationCreationRequest);

    OrganisationsDetailResponse retrieveOrganisations();

    OrganisationResponse updateOrganisation(OrganisationCreationRequest organisationCreationRequest, UUID organisationIdentifier);

    Organisation getOrganisationByOrganisationIdentifier(UUID organisationIdentifier);

}

