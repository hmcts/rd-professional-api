package uk.gov.hmcts.reform.professionalapi.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

import java.time.LocalDateTime;
import java.util.List;
import javax.validation.constraints.NotNull;

@Setter
@Getter
@NoArgsConstructor
public class OrganisationByProfileResponse {
    private String organisationIdentifier;
    private String status;
    private LocalDateTime lastUpdated;
    private List<String> organisationProfileIds;

    @NotNull
    public OrganisationByProfileResponse(Organisation organisation) {
        this.organisationIdentifier = organisation.getOrganisationIdentifier();
        this.status = organisation.getStatus().name();
        this.lastUpdated = organisation.getLastUpdated();
        this.organisationProfileIds = RefDataUtil.getOrganisationProfileIds(organisation);
    }
}
