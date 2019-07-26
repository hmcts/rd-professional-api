package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderMethodName = "aNewUserCreationRequest")
public class NewUserCreationRequest {

    @JsonIgnore
    private final String emailRegex = "\\A(?=[a-zA-Z0-9@.!#$%&'*+/=?^_`{|}~-]{6,254}\\z)(?=[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]{1,64}@)[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:(?=[a-zA-Z0-9-]{1,63}\\.)[a-zA-Z0-9](?:[a-z0-9-]*[a-zA-Z0-9])?\\.)+(?=[a-zA-Z0-9-]{1,63}\\z)[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\z";

    @NotNull
    private final String firstName;
    @NotNull
    private final String lastName;

    @NotNull
    @Pattern(regexp = emailRegex)
    private final String email;

    @NotNull
    private final List<String> roles;

    @JsonCreator
    public NewUserCreationRequest(
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("email") String emailAddress,
            @JsonProperty("roles") List<String> roles) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = emailAddress.toLowerCase();
        this.roles = roles;
    }
}