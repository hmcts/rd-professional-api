package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProfessionalUsersEntityResponse {

    @JsonProperty
    private String organisationIdentifier;
    private List<ProfessionalUsersResponse> users = new ArrayList<>();
}
