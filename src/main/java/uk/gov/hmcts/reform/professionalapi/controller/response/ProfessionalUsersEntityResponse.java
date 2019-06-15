package uk.gov.hmcts.reform.professionalapi.controller.response;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import lombok.Getter;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

@Getter
public class ProfessionalUsersEntityResponse {
    @JsonProperty
    private final List<ProfessionalUsersResponse> users;

    public ProfessionalUsersEntityResponse(List<ProfessionalUser> professionalUsers) {
        this.users = professionalUsers.stream()
            .map(professionalUser -> new ProfessionalUsersResponse(professionalUser))
            .collect(toList());
    }
}
