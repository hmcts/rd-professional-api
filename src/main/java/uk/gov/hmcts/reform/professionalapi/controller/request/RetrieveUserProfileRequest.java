package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.UUID;

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