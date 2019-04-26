package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.hmcts.reform.professionalapi.domain.entities.Organisation;

public class OrganisationResponse {

    @JsonProperty
    private final String organisationIdentifier;
	
    public OrganisationResponse(Organisation organisation) {
    	
        this.organisationIdentifier = organisation.getOrganisationIdentifier().toString();
		
    }

    public String getOrganisationIdentifier() {
    	
		return organisationIdentifier;
	}

}
