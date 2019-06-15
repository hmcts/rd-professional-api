package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

@Getter
public class OrganisationPbaResponse {

    @JsonProperty
    private final OrganisationEntityResponse organisationEntityResponse;

    public OrganisationPbaResponse(Organisation organisation, Boolean isRequiredAllEntities) {

        this.organisationEntityResponse = new OrganisationEntityResponse(organisation, isRequiredAllEntities);
    }

}
