package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganisationsWithPbaStatusResponse {

    @JsonProperty
    private String organisationIdentifier;

    @JsonProperty
    private OrganisationStatus status;

    @JsonProperty
    private List<FetchPbaByStatusResponse> pbaNumbers;

    @JsonProperty
    private String organisationName;

    @JsonProperty
    private SuperUserResponse superUser;

}
