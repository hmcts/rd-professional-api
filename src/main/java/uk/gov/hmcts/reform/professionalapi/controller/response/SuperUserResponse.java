package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.util.PbaAccountUtil;

public class SuperUserResponse {

    @JsonProperty
    private String userIdentifier;
    @JsonProperty
    private String firstName;
    @JsonProperty
    private String lastName;
    @JsonProperty
    private String email;


    public SuperUserResponse(ProfessionalUser professionalUser) {

        getSuperUserResponse(professionalUser);

    }

    private void getSuperUserResponse(ProfessionalUser professionalUser) {
        this.userIdentifier = StringUtils.isEmpty(professionalUser.getUserIdentifier())
                ? "" : professionalUser.getUserIdentifier().toString();
        this.firstName = PbaAccountUtil.removeEmptySpaces(professionalUser.getFirstName());
        this.lastName = PbaAccountUtil.removeEmptySpaces(professionalUser.getLastName());
        this.email = PbaAccountUtil.removeEmptySpaces(professionalUser.getEmailAddress());
    }
}
