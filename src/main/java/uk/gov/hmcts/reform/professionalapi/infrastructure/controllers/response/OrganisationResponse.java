package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.response;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.hmcts.reform.professionalapi.domain.entities.Organisation;

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
