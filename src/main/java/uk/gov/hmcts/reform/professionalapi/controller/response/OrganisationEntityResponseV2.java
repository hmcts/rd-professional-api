package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.ObjectUtils;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

import java.util.List;

import static java.util.Objects.nonNull;

public class OrganisationEntityResponseV2 extends OrganisationEntityResponse {
    @JsonProperty
    private String orgType;

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
        this.organisationIdentifier = ObjectUtils.isEmpty(organisation.getOrganisationIdentifier())
                ? "" : organisation.getOrganisationIdentifier();
        this.name = organisation.getName();
        this.status = organisation.getStatus();
        this.statusMessage = organisation.getStatusMessage();
        this.sraId = organisation.getSraId();
        this.sraRegulated = organisation.getSraRegulated();
        this.orgType = organisation.getOrgType();
        this.companyNumber = organisation.getCompanyNumber();
        this.companyUrl = organisation.getCompanyUrl();
        if (!organisation.getUsers().isEmpty()) {
            this.superUser = new SuperUserResponse(organisation.getUsers().get(0));
        }

        if (Boolean.TRUE.equals(isRequiredOrAttribute)) {
            this.orgAttributes = organisation.getOrgAttributes()
                    .stream()
                    .map(OrgAttributeResponse::new)
                    .toList();
        }
    }
}