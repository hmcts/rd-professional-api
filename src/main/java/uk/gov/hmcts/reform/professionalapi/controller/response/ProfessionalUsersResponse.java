package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProfessionalUsersResponse extends ProfessionalUsersResponseWithoutRoles {

    @JsonProperty
    private List<String> roles;
    @JsonProperty
    private String idamStatusCode;
    @JsonProperty
    private String idamMessage;

    public ProfessionalUsersResponse(ProfessionalUser user) {
        if (user.getUserIdentifier() != null) {
            this.userIdentifier = user.getUserIdentifier().toString();
        }

        this.userIdentifier = userIdentifier;
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmailAddress();
        this.roles = user.getRoles();
        this.idamStatus = user.getIdamStatus() ==  null ? "" :  user.getIdamStatus().toString();
        this.idamStatusCode = StringUtils.isBlank(user.getIdamStatusCode()) ? "" : user.getIdamStatusCode();
        this.idamMessage = StringUtils.isBlank(user.getIdamMessage()) ? "" : user.getIdamMessage();
    }
}
