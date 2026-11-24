package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Builder(builderMethodName = "aRetrieveUserProfilesRequest")
public class RetrieveUserProfilesRequest {

    @NotEmpty
    private List<String> userIds;

    @JsonCreator
    public RetrieveUserProfilesRequest(@JsonProperty(value = "userIds") List<String> userIds) {

        this.userIds = userIds;
    }
}
