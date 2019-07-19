package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.List;
import java.util.UUID;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder(builderMethodName = "aRetrieveUserProfilesRequest")
public class RetrieveUserProfilesRequest {

    @NotEmpty
    private List<UUID> userIds;

    @JsonCreator
    public RetrieveUserProfilesRequest(
            @JsonProperty(value = "userIds") List<UUID> userIds) {

        this.userIds = userIds;
    }
}
