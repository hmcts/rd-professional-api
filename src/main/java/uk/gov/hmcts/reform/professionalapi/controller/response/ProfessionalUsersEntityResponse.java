package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class ProfessionalUsersEntityResponse {

    @JsonProperty
    @Setter
    private String organisationIdentifier;

    @JsonProperty
    private List<ProfessionalUsersResponse> users;

    @JsonSetter("userProfiles")
    public void setUserProfiles(List<ProfessionalUsersResponse> userProfiles) {
        this.users = userProfiles;
    }
}
