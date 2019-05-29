package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

public class OrganisationResponse {

    @JsonProperty
    private final String organisationIdentifier;

    public OrganisationResponse(Organisation organisation) {

        this.organisationIdentifier = organisation.getOrganisationIdentifier();
    }

    public String getOrganisationIdentifier() {

        return organisationIdentifier;
    }

}
