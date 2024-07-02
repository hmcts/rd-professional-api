package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

import java.util.List;

public class OrganisationsDetailResponse {

    @JsonProperty
    @Getter
    private final List<OrganisationEntityResponse> organisations;

    @JsonIgnore
    @Getter
    @Setter
    long totalRecords;

    @Getter
    @Setter
    boolean moreAvailable;

    public OrganisationsDetailResponse(
            List<Organisation> organisations, Boolean isRequiredContactInfo,
            Boolean isRequiredPendingPbas, Boolean isRequiredAllPbas) {

        this.organisations = organisations.stream().map(organisation ->
                new OrganisationEntityResponse(organisation, isRequiredContactInfo,
                        isRequiredPendingPbas, isRequiredAllPbas)).toList();
    }
}
