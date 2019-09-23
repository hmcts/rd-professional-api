package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderMethodName = "aNewUserCreationRequest")
public class NewUserCreationRequest {

    @NotNull
    private final String firstName;
    @NotNull
    private final String lastName;

    @NotNull
    private final String email;

    @NotNull
    private final List<String> roles;

    private final List<Jurisdiction> jurisdictions;

    @JsonCreator
    public NewUserCreationRequest(
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("email") String emailAddress,
            @JsonProperty("roles") List<String> roles,
            @JsonProperty("jurisdictions") List<Jurisdiction> jurisdictions) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = emailAddress.toLowerCase();
        this.roles = roles;
        this.jurisdictions = jurisdictions;
    }
}