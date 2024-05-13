package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder(builderMethodName = "userDeletionRequest")
public class UserDeletionRequest {

    private List<String> emails;

    @JsonCreator
    public UserDeletionRequest(
        @JsonProperty("emails") List<String> emails
    ) {
        this.emails = emails;
    }
}