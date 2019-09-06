package uk.gov.hmcts.reform.professionalapi.controller.response;

import static java.util.Objects.requireNonNull;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
        if (isRequiredRoles) {
            this.roles = getRoles();
            this.idamStatusCode = getIdamStatusCode();
            this.idamMessage = getIdamMessage();
        }
    }
}

