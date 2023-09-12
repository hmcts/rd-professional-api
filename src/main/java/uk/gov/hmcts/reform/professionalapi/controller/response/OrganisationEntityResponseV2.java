package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

import java.util.List;

import static java.util.Objects.nonNull;

public class OrganisationEntityResponseV2 extends OrganisationEntityResponse {
    @JsonProperty
    private String orgTypeKey;

    @JsonProperty
    private List<OrgAttributeResponse> orgAttributes;

    public OrganisationEntityResponseV2(
            Organisation organisation, Boolean isRequiredContactInfo,
            Boolean isRequiredPendingPbas, Boolean isRequiredAllPbas,Boolean isRequiredOrAttribute) {
        super(organisation,isRequiredContactInfo,isRequiredPendingPbas,isRequiredAllPbas);

        if (nonNull(organisation)) {
            getOrganisationEntityResponse(
                    organisation, isRequiredContactInfo,
                    isRequiredPendingPbas,
                    isRequiredAllPbas,
                    isRequiredOrAttribute);


        }
    }


    @SuppressWarnings("java:S6204")
    private void getOrganisationEntityResponse(Organisation organisation,
                                               Boolean isRequiredContactInfo, Boolean isRequiredPendingPbas,
                                               Boolean isRequiredAllPbas, Boolean isRequiredOrAttribute) {
        getOrganisationEntityResponse(
                organisation, isRequiredContactInfo, isRequiredPendingPbas, isRequiredAllPbas);

        this.orgTypeKey = organisation.getOrgTypeKey();

        if (Boolean.TRUE.equals(isRequiredOrAttribute)) {
            this.orgAttributes = organisation.getOrgAttributes()
                    .stream()
                    .map(OrgAttributeResponse::new)
                    .toList();
        }
    }
}