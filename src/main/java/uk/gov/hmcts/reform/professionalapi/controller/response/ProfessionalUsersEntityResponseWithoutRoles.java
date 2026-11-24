package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class ProfessionalUsersEntityResponseWithoutRoles {

    @JsonProperty
    @Setter
    private String organisationIdentifier;

    @JsonProperty
    @Setter
    private String organisationStatus;

    @JsonProperty
    @Setter
    private List<String> organisationProfileIds;

    private List<ProfessionalUsersResponseWithoutRoles> userProfiles = new ArrayList<>();

    @JsonGetter("users")
    public List<ProfessionalUsersResponseWithoutRoles> getUserProfiles() {
        return userProfiles;
    }

    @JsonSetter("userProfiles")
    public void setUserProfiles(List<ProfessionalUsersResponseWithoutRoles> userProfiles) {
        this.userProfiles = userProfiles;
    }
}
