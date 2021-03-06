package uk.gov.hmcts.reform.professionalapi.service;

import java.util.List;
import java.util.Set;

import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.DeleteOrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;


public interface OrganisationService {

    OrganisationResponse createOrganisationFrom(OrganisationCreationRequest organisationCreationRequest);

    OrganisationsDetailResponse retrieveAllOrganisations();

    OrganisationEntityResponse retrieveOrganisation(String organisationIdentifier);

    OrganisationResponse updateOrganisation(OrganisationCreationRequest organisationCreationRequest,
                                            String organisationIdentifier);

    Organisation getOrganisationByOrgIdentifier(String organisationIdentifier);

    OrganisationsDetailResponse findByOrganisationStatus(OrganisationStatus status);

    DeleteOrganisationResponse deleteOrganisation(Organisation organisation, String userId);

    List<Organisation> getOrganisationByStatus(OrganisationStatus status);

    void addPbaAccountToOrganisation(Set<String> paymentAccounts, Organisation organisation, boolean pbasValidated);
}

