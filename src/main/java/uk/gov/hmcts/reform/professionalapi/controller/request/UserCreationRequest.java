package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
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

    private List<Jurisdiction> jurisdictions;

    @JsonCreator
    public UserCreationRequest(
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("email") String emailAddress,
            @JsonProperty("jurisdictions") List<Jurisdiction> jurisdictions
    ) {

        this.firstName = firstName;
        this.lastName = lastName;

        if (!StringUtils.isEmpty(emailAddress)) {

            this.email = emailAddress.toLowerCase().trim();
        }
        this.jurisdictions = jurisdictions;

    }


}

