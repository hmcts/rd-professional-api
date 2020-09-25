package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.professionalapi.domain.Jurisdiction;

@Getter
@Setter
@Builder(builderMethodName = "aUserCreationRequest")
public class UserCreationRequest {

    @NotNull
    private final String firstName;

    @NotNull
    private final String lastName;

    private String email;


    @JsonCreator
    public UserCreationRequest(
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("email") String emailAddress) {

        this.firstName = firstName;
        this.lastName = lastName;

        if (!StringUtils.isEmpty(emailAddress)) {

            this.email = emailAddress.toLowerCase().trim();
        }
    }


}

