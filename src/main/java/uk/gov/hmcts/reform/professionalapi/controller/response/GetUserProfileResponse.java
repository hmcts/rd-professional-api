package uk.gov.hmcts.reform.professionalapi.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;

import java.util.List;

import static java.util.Objects.requireNonNull;

@Setter
@Getter
@NoArgsConstructor
public class GetUserProfileResponse {

    private String idamId;
    private String firstName;
    private String lastName;
    private String email;
    private IdamStatus idamStatus;
    private List<String> roles;
    private String idamStatusCode;
    private String idamMessage;

    public GetUserProfileResponse(UserProfile userProfile, Boolean isRequiredRoles) {
        requireNonNull(userProfile, "userProfile must not be null");
        this.idamId = userProfile.getIdamId();
        this.firstName = userProfile.getFirstName();
        this.lastName = userProfile.getLastName();
        this.email = userProfile.getEmail();
        this.idamStatus = userProfile.getIdamStatus();
        if (Boolean.TRUE.equals(isRequiredRoles)) {
            this.roles = getRoles();
            this.idamStatusCode = getIdamStatusCode();
            this.idamMessage = getIdamMessage();
        }
    }
}

