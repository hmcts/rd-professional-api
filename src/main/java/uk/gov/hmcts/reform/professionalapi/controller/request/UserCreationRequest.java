package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import lombok.Builder;
import lombok.Getter;
import org.springframework.util.StringUtils;



@Getter
@Builder(builderMethodName = "aUserCreationRequest")
public class UserCreationRequest {

    @JsonIgnore
    private final String emailRegex = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^-]+(?:\\.[a-zA-Z0-9_!#$%&’*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";

    @NotNull
    private final String firstName;

    @NotNull
    private final String lastName;

    @Pattern(regexp = emailRegex)
    private String email;

    @JsonCreator
    public UserCreationRequest(
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("email") String emailAddress
    ) {

        this.firstName = firstName;
        this.lastName = lastName;

        if (!StringUtils.isEmpty(emailAddress)) {

            this.email = emailAddress.toLowerCase().trim();
        }

    }


}

