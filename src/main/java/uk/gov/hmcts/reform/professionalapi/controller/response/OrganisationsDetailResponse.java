package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

public class OrganisationsDetailResponse {

    @JsonProperty
    private  List<OrganisationEntityResponse> organisations;

    public OrganisationsDetailResponse(List<Organisation> organisations, Boolean isRequiredAllEntities) {

        this.organisations = organisations.stream().map(organisation ->
                new OrganisationEntityResponse(organisation, isRequiredAllEntities)).collect(Collectors.toList());

    }

    public List<OrganisationEntityResponse> getOrganisations() {
        return organisations;
    }

}
