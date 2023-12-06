package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class RefreshUser {

    private String userIdentifier;
    private LocalDateTime lastUpdated;
    private String organisationIdentifier;
    private List<AccessType> accessTypes;

    public RefreshUser(String userIdentifier,
                       LocalDateTime lastUpdated,
                       String organisationIdentifier,
                       List<AccessType> accessTypes) {
        this.userIdentifier = userIdentifier;
        this.lastUpdated = lastUpdated;
        this.organisationIdentifier = organisationIdentifier;
        this.accessTypes = accessTypes;
    }
}
