package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

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
