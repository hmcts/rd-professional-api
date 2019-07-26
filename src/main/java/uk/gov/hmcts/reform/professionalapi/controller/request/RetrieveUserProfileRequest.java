package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
import javax.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder(builderMethodName = "aRetrieveUserProfileRequest")
public class RetrieveUserProfileRequest {

    @NotEmpty
    private UUID userId;

    @JsonCreator
    public RetrieveUserProfileRequest(
            @JsonProperty(value = "userIds") UUID userId) {

        this.userId = userId;
    }
}