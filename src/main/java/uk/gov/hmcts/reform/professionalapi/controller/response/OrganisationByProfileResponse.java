package uk.gov.hmcts.reform.professionalapi.controller.response;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class OrganisationByProfileResponse {
    private String organisationIdentifier;
    private String status;
    private LocalDateTime lastUpdated;
    private List<String> organisationProfileId;

    public OrganisationByProfileResponse(Organisation organisation) {
        this.organisationIdentifier = organisation.getOrganisationIdentifier();
        this.status = organisation.getStatus().name();
        this.lastUpdated = organisation.getLastUpdated();
        this.organisationProfileId = RefDataUtil.getOrganisationProfileIds(organisation);
    }
}
