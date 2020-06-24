package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.ArrayList;
import java.util.List;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ProfessionalUsersEntityResponseWithoutRoles {

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
