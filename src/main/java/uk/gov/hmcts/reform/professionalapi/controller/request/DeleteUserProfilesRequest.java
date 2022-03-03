package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;
import javax.validation.constraints.NotEmpty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder(builderMethodName = "aDeleteUserProfilesRequest")
@AllArgsConstructor
public class DeleteUserProfilesRequest {

    @NotEmpty
    @JsonProperty
    private Set<String> userIds;

}
