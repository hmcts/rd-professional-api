package uk.gov.hmcts.reform.professionalapi.controller.response;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class GetUserProfileResponse {

    private UUID idamId;
    private String firstName;
    private String lastName;
    private String email;
    private IdamStatus idamStatus;
    private List<String> roles;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer idamErrorStatusCode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String idamErrorMessage;

    public GetUserProfileResponse(UserProfile userProfile, Boolean isRequiredRoles) {
        requireNonNull(userProfile, "userProfile must not be null");
        this.idamId = userProfile.getIdamId();
        this.firstName = userProfile.getFirstName();
        this.lastName = userProfile.getLastName();
        this.email = userProfile.getEmail();
        this.idamStatus = userProfile.getIdamStatus();
        if (isRequiredRoles) {
            this.roles = getRoles();
            this.idamErrorStatusCode = getIdamErrorStatusCode();
            this.idamErrorMessage = getIdamErrorMessage();
        }
    }
}

