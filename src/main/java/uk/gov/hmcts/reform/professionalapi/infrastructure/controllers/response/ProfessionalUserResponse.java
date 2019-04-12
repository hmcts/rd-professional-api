package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.hmcts.reform.professionalapi.domain.entities.ProfessionalUser;

public class ProfessionalUserResponse {

    @JsonProperty
    private final String id;
    @JsonProperty
    private final String firstName;
    @JsonProperty
    private final String lastName;
    @JsonProperty
    private final String email;

    public ProfessionalUserResponse(ProfessionalUser user) {
        this.id = user.getId().toString();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmailAddress();
    }
}
