package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

@Getter
public class OrganisationPbaResponseV2 {

    @JsonProperty
    private final OrganisationEntityResponseV2 organisationEntityResponse;

    public OrganisationPbaResponseV2(
            Organisation organisation, Boolean isRequiredContactInfo,
            Boolean isRequiredPendingPbas, Boolean isRequiredAllPbas,Boolean isRequiredOrAttribute) {

        this.organisationEntityResponse = new OrganisationEntityResponseV2(organisation, isRequiredContactInfo,
                        isRequiredPendingPbas, isRequiredAllPbas, isRequiredOrAttribute);
    }

}
