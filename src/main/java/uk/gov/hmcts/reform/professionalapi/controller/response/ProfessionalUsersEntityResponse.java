package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProfessionalUsersEntityResponse {

    @JsonProperty
    private String organisationIdentifier;
    @JsonProperty
    private List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
    
    public List<ProfessionalUsersResponse> getUserProfiles() {
        return userProfiles;
    }
    
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
