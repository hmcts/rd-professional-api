package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.reform.professionalapi.util.PbaAccountUtil;

@Getter
@Builder(builderMethodName = "aNewUserCreationRequest")
public class NewUserCreationRequest {

    @JsonIgnore
    private final String emailRegex = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&’*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";

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

        this.firstName = PbaAccountUtil.removeEmptySpaces(firstName);
        this.lastName = PbaAccountUtil.removeEmptySpaces(lastName);
        this.email = PbaAccountUtil.removeAllSpaces(emailAddress.toLowerCase());
        this.roles = roles;
    }
}