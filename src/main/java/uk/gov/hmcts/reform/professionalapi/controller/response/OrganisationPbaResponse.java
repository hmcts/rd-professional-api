package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

public class OrganisationPbaResponse {


    @JsonProperty
    private OrganisationEntityResponse organisationEntityResponse;

    public OrganisationPbaResponse(Organisation organisation, Boolean isRequiredAllEntities) {

        getOrganisationPbaResponse(organisation, isRequiredAllEntities);
    }

    private void getOrganisationPbaResponse(Organisation organisation, Boolean isRequiredAllEntities) {

        this.organisationEntityResponse = new OrganisationEntityResponse(organisation, isRequiredAllEntities);

    }

}
