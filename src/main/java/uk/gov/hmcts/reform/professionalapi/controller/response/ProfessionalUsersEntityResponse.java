package uk.gov.hmcts.reform.professionalapi.controller.response;

import static java.util.stream.Collectors.toList;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

@NoArgsConstructor
public class ProfessionalUsersEntityResponse {
    @JsonProperty
    private List<ProfessionalUserResponse> users;

    public ProfessionalUsersEntityResponse(List<ProfessionalUser> professionalUsers) {
        this.users = professionalUsers.stream()
            .map(professionalUser -> new ProfessionalUserResponse(professionalUser))
            .collect(toList());
    }
}
