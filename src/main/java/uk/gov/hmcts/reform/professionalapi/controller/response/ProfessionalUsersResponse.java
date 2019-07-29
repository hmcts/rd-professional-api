package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

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
    private List<String> roles;
    @JsonProperty
    private IdamStatus idamStatus;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer idamErrorStatusCode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String idamErrorMessage;

    public ProfessionalUsersResponse(ProfessionalUser user) {
        this.userIdentifier = user.getUserIdentifier();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmailAddress();
        this.idamStatus = user.getIdamStatus();
        this.idamErrorStatusCode = user.getIdamErrorStatusCode();
        this.idamErrorMessage = user.getIdamErrorMessage();
    }
}
