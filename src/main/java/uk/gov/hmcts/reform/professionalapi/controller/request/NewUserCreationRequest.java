package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.professionalapi.domain.Jurisdiction;

@Getter
@Setter
@Builder(builderMethodName = "aNewUserCreationRequest")
public class NewUserCreationRequest {

    private String firstName;
    private String lastName;
    private String email;
    private List<String> roles;
    private List<Jurisdiction> jurisdictions;
    private boolean resendInvite;

    @JsonCreator
    public NewUserCreationRequest(
            @JsonProperty("firstName") String firstName,
            @JsonProperty("lastName") String lastName,
            @JsonProperty("email") String emailAddress,
            @JsonProperty("roles") List<String> roles,
            @JsonProperty("jurisdictions") List<Jurisdiction> jurisdictions,
            @JsonProperty("resendInvite") boolean resendInvite) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = StringUtils.isBlank(emailAddress) ? emailAddress : emailAddress.toLowerCase();
        this.roles = roles;
        this.jurisdictions = jurisdictions;
        this.resendInvite = resendInvite;
    }
}