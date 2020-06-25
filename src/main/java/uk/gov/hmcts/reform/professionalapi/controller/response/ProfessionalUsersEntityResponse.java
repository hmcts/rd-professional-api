package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;
import java.util.List;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProfessionalUsersEntityResponse {

    @JsonProperty
    private String organisationIdentifier;
    private List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
    
    @JsonGetter("users")
    public List<ProfessionalUsersResponse> getUserProfiles() {
        return userProfiles;
    }
    
    @JsonSetter("userProfiles")
    public void setUserProfiles(List<ProfessionalUsersResponse> userProfiles) {
        this.userProfiles = userProfiles;
    }

    public void setOrganisationIdentifier(String organisationIdentifier) {
        this.organisationIdentifier = organisationIdentifier;
    }

    public String getOrganisationIdentifier() {
        return this.organisationIdentifier;
    }
}
