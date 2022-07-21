package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;

import java.util.List;

@Getter
@Setter
public class MultipleUserProfilesResponse {

    @JsonProperty
    private List<GetUserProfileResponse> userProfiles;

    public MultipleUserProfilesResponse(List<UserProfile> userProfiles, Boolean isRequiredRoles) {

        this.userProfiles = userProfiles.stream().map(userProfile ->
                new GetUserProfileResponse(userProfile, isRequiredRoles)).toList();

    }
}