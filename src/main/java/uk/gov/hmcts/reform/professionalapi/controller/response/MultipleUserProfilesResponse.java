package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MultipleUserProfilesResponse {

    @JsonProperty
    private List<GetUserProfileResponse> userProfiles;

    public MultipleUserProfilesResponse(List<UserProfile> userProfiles, Boolean isRequiredRoles) {

        this.userProfiles = userProfiles.stream().map(userProfile ->
                new GetUserProfileResponse(userProfile, isRequiredRoles)).collect(Collectors.toList());

    }
}