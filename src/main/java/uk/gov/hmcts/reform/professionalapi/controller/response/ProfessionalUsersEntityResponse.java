package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class ProfessionalUsersEntityResponse {

    @JsonProperty
    @Setter
    private String organisationIdentifier;

    @JsonProperty
    @Setter
    private String organisationStatus;

    @JsonProperty
    @Setter
    private List<String> organisationProfileIds;

    @JsonProperty
    private List<ProfessionalUsersResponse> users = new ArrayList<>();

    @JsonSetter("userProfiles")
    public void setUserProfiles(List<ProfessionalUsersResponse> userProfiles) {
        this.users = userProfiles;
    }
}
