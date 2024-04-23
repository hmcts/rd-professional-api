package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "userDeletionRequest")
public class UserDeletionRequest {

    private String firstName;
    private String lastName;
    private List<String> emails;

    @JsonCreator
    public UserDeletionRequest(
        @JsonProperty("firstName") String firstName,
        @JsonProperty("lastName") String lastName,
        @JsonProperty("emails") List<String> emails
    ) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.emails = emails;
    }
}