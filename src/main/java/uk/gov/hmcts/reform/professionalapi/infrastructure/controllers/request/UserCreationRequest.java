package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderMethodName = "aUserCreationRequest")
public class UserCreationRequest {

    @NotNull
    private final String firstName;
    @NotNull
    private final String lastName;
    @NotNull
    private final String email;

    @JsonCreator
    public UserCreationRequest(

            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("email") String email) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
}
