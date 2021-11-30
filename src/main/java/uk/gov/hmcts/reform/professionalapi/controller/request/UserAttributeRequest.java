package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderMethodName = "aUserAttributeCreationRequest")
public class UserAttributeRequest {

    @JsonProperty
    private List<String> userRoles;

    @JsonCreator
    public UserAttributeRequest(
            @JsonProperty("userRoles") List<String> userRoles) {

        this.userRoles = userRoles;
    }

}
