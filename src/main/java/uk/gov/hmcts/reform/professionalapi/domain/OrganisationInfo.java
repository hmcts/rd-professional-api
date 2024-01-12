package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class OrganisationInfo {

    private String organisationIdentifier;
    private OrganisationStatus status;
    private LocalDateTime lastUpdated;
    private List<String> organisationProfileIds;

    public OrganisationInfo(String organisationIdentifier,
                            OrganisationStatus status,
                            LocalDateTime lastUpdated,
                            List<String> organisationProfileIds) {
        this.organisationIdentifier = organisationIdentifier;
        this.status = status;
        this.lastUpdated = lastUpdated;
        this.organisationProfileIds = organisationProfileIds;
    }
}
