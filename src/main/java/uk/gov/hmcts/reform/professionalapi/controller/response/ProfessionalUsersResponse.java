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
        super(user);
        this.roles = user.getRoles();
        this.idamStatusCode = StringUtils.isBlank(user.getIdamStatusCode()) ? "" : user.getIdamStatusCode();
        this.idamMessage = StringUtils.isBlank(user.getIdamMessage()) ? "" : user.getIdamMessage();
    }
}
