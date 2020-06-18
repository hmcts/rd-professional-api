package uk.gov.hmcts.reform.professionalapi.controller.response;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;
import java.util.List;

import lombok.NoArgsConstructor;

import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

@NoArgsConstructor
public class ProfessionalUsersEntityResponse {

    @JsonProperty
    private String organisationIdentifier;
    private List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();

    public ProfessionalUsersEntityResponse(List<ProfessionalUser> professionalUsers) {
        this.userProfiles = professionalUsers.stream()
            .map(professionalUser -> new ProfessionalUsersResponse(professionalUser))
            .collect(toList());
    }
    
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
}
