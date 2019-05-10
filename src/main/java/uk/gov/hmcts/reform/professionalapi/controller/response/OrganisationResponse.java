package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

public class OrganisationResponse {

    @JsonProperty
    private final UUID organisationIdentifier;

    public OrganisationResponse(Organisation organisation) {

        this.organisationIdentifier = organisation.getOrganisationIdentifier();
    }

    public UUID getOrganisationIdentifier() {

        return organisationIdentifier;
    }

}
