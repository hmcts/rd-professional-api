package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;

@Getter
@Setter
@NoArgsConstructor
public class ProfessionalUsersResponse {

    @JsonProperty
    private UUID userIdentifier;
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

    public ProfessionalUsersResponse(ProfessionalUser user) {
        this.userIdentifier = Optional.ofNullable(user.getUserIdentifier()).orElse(user.getUserIdentifier());
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmailAddress();
        this.status = user.getStatus();
    }
}