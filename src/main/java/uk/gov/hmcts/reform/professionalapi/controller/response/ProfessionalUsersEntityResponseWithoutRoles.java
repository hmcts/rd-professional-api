package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class ProfessionalUsersEntityResponseWithoutRoles {

    @JsonProperty
    private String organisationIdentifier;

    private List<ProfessionalUsersResponseWithoutRoles> userProfiles = new ArrayList<>();

    @JsonGetter("users")
    public List<ProfessionalUsersResponseWithoutRoles> getUserProfiles() {
        return userProfiles;
    }

    @JsonSetter("userProfiles")
    public void setUserProfiles(List<ProfessionalUsersResponseWithoutRoles> userProfiles) {
        this.userProfiles = userProfiles;
    }

    public String getOrganisationIdentifier() {
        return organisationIdentifier;
    }

    public void setOrganisationIdentifier(String organisationIdentifier) {
        this.organisationIdentifier = organisationIdentifier;
    }
}
