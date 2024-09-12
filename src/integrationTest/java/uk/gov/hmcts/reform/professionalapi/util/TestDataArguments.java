package uk.gov.hmcts.reform.professionalapi.util;

import lombok.Builder;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationNameUpdateRequest;

@Builder
public record TestDataArguments(
        String statusCode,
        String errorMessage,
        String organisationIdentifier,
        String orgName,
        OrganisationNameUpdateRequest organisationNameUpdateRequest,
        boolean validOrgIdIsRequired) {}
