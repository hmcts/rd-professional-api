package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;

public class ProfessionalUserResponse {

    @JsonProperty
    private String userIdentifier;
    @JsonProperty
    private String firstName;
    @JsonProperty
    private String lastName;
    @JsonProperty
    private String email;
    @JsonProperty
    private ProfessionalUserStatus status;
    @JsonProperty
    private List<String> roles = new ArrayList<String>();

    public ProfessionalUserResponse(ProfessionalUser user) {
        this.userIdentifier = StringUtils.isEmpty(user.getUserIdentifier())
                ? "" : user.getUserIdentifier().toString();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmailAddress();
        this.status = user.getStatus();
    }
}
