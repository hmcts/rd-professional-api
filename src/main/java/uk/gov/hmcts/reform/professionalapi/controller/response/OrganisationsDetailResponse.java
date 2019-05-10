package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

@NoArgsConstructor
public class OrganisationsDetailResponse {

    @JsonProperty
    private  List<OrganisationEntityResponse> organisations;

    public OrganisationsDetailResponse(List<Organisation> organisations) {

        this.organisations = organisations.stream().map(organisation ->
                new OrganisationEntityResponse(organisation)).collect(Collectors.toList());

    }
}
