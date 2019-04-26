package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.hmcts.reform.professionalapi.domain.entities.Organisation;

public class OrganisationResponse {

    @JsonProperty
    private final String id;
    @JsonProperty
    private final String name;
    @JsonProperty
    private final List<String> userIds;
    @JsonProperty
    private final List<String> pbaAccounts;

    public OrganisationResponse(Organisation organisation) {
        this.id = organisation.getId().toString();
        this.name = organisation.getName();
        this.userIds = organisation.getUsers()
                .stream()
                .map(user -> user.getId().toString())
                .collect(toList());
        this.pbaAccounts = organisation.getPaymentAccounts()
                .stream()
                .map(acc -> acc.getPbaNumber())
                .collect(toList());
    private final String organisationIdentifier;
	
    public OrganisationResponse(Organisation organisation) {
    	
        this.organisationIdentifier = organisation.getOrganisationIdentifier().toString();
		
    }

    public String getOrganisationIdentifier() {
    	
		return organisationIdentifier;
	}

}
