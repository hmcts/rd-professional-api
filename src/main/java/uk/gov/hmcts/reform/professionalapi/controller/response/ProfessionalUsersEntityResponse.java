package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;
import java.util.List;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProfessionalUsersEntityResponse {
    private List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
    
    @JsonGetter("users")
    public List<ProfessionalUsersResponse> getUserProfiles() {
        return userProfiles;
    }
    
    @JsonSetter("userProfiles")
    public void setUserProfiles(List<ProfessionalUsersResponse> userProfiles) {
        this.userProfiles = userProfiles;
    }
}
