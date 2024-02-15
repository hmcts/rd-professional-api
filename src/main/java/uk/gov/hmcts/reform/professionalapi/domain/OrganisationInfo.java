package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrganisationInfo {

    private String organisationIdentifier;
    private OrganisationStatus status;
    private LocalDateTime lastUpdated;
    private List<String> organisationProfileIds;
}
