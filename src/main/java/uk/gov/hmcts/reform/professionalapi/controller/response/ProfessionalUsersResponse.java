package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

@Getter
@Setter
@NoArgsConstructor
public class ProfessionalUsersResponse {

    @JsonProperty("userIdentifier")
    private UUID idamId;
    @JsonProperty
    private String firstName;
    @JsonProperty
    private String lastName;
    @JsonProperty
    private String email;
    @JsonProperty
    private List<String> roles;

    public ProfessionalUsersResponse(ProfessionalUser user) {
        this.idamId = Optional.ofNullable(user.getUserIdentifier()).orElse(user.getUserIdentifier());
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmailAddress();
    }
   
//    @JsonGetter("userIdentifier")
//    public UUID getUserIdentifier() {
//        return idamId;
//    }
}
