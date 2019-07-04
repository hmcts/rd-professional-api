package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.Getter;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

@Getter
public class ProfessionalUsersResponse {

    @JsonProperty
    private final UUID userIdentifier;
    @JsonProperty
    private final String firstName;
    @JsonProperty
    private final String lastName;
    @JsonProperty
    private final String email;
    @JsonProperty
    private final List<String> roles = new ArrayList<String>();

    public ProfessionalUsersResponse(ProfessionalUser user) {
        this.userIdentifier = Optional.ofNullable(user.getUserIdentifier()).orElse(user.getUserIdentifier());
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmailAddress();
    }
}