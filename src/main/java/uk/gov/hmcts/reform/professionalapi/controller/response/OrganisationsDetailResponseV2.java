package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

import java.util.List;

public class OrganisationsDetailResponseV2 {

    @JsonProperty
    @Getter
    private final List<OrganisationEntityResponseV2> organisations;

    @JsonIgnore
    @Getter
    @Setter
    long totalRecords;

    @Getter
    @Setter
    boolean moreAvailable;

    public OrganisationsDetailResponseV2(List<Organisation> organisations,
                                         Boolean isRequiredContactInfo,
                                         Boolean isRequiredPendingPbas,
                                         Boolean isRequiredAllPbas,
                                         Boolean isRequiredOrgAttribute) {

        this.organisations = organisations.stream().map(organisation ->
                new OrganisationEntityResponseV2(organisation, isRequiredContactInfo,
                        isRequiredPendingPbas, isRequiredAllPbas, isRequiredOrgAttribute)).toList();
    }
}

