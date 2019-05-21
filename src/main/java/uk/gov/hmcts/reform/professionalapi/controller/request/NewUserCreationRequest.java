package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;

import uk.gov.hmcts.reform.professionalapi.domain.UserRoles;

@Getter
@Builder(builderMethodName = "aUserCreationRequest")
public class NewUserCreationRequest {

    @NotNull
    private final String firstName;
    @NotNull
    private final String lastName;
    @NotNull
    private final String email;
    @NotNull
    private final String status;
    @Nullable
    private final List<String> roles;

    @JsonCreator
    public NewUserCreationRequest(
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("email") String email,
            @JsonProperty("status") String status,
            @JsonProperty("roles") List<String> roles) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.status = status;
        this.roles = roles;
    }
}