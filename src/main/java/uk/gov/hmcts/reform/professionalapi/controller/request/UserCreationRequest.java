package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Builder(builderMethodName = "aUserCreationRequest")
public class UserCreationRequest {

    @NotNull
    private final String firstName;

    @NotNull
    private final String lastName;

    private  String email;

    private List<Map<String,String>> jurisdictions;

    @JsonCreator
    public UserCreationRequest(
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("email") String emailAddress,
            @JsonProperty("jurisdictions") List<Map<String,String>> jurisdictions
    ) {

        this.firstName = firstName;
        this.lastName = lastName;
        if (!StringUtils.isEmpty(emailAddress)) {

            this.email = emailAddress.toLowerCase().trim();
        }
        this.jurisdictions = jurisdictions;

    }
}
