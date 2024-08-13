package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccessType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ProfessionalUsersResponseWithoutRoles {

    @JsonProperty
    public String userIdentifier;
    @JsonProperty
    public String firstName;
    @JsonProperty
    public String lastName;
    @JsonProperty
    public String email;
    @JsonProperty
    public String idamStatus;
    @JsonProperty
    public LocalDateTime lastUpdated;
    @JsonProperty
    public List<UserAccessType> userAccessTypes = new ArrayList<>();
    public UUID userUuid;

    public ProfessionalUsersResponseWithoutRoles(ProfessionalUser user) {
        this.userIdentifier = user.getUserIdentifier() == null ? null : user.getUserIdentifier().toString();
        this.userUuid = user.getUserIdentifier();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmailAddress();
        this.idamStatus = user.getIdamStatus() == null ? "" : user.getIdamStatus().toString();
        this.lastUpdated = user.getLastUpdated();
    }

    public UUID getUserIdentifierUuid() {
        return userUuid;
    }
}
