package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

import java.util.List;

public class OrganisationsDetailResponse {

    @JsonProperty
    private final List<OrganisationEntityResponse> organisations;

    public OrganisationsDetailResponse(
            List<Organisation> organisations, Boolean isRequiredContactInfo,
            Boolean isRequiredPendingPbas, Boolean isRequiredAllPbas) {

        this.organisations = organisations.stream().map(organisation ->
                new OrganisationEntityResponse(organisation, isRequiredContactInfo,
                        isRequiredPendingPbas, isRequiredAllPbas)).toList();
    }
}
