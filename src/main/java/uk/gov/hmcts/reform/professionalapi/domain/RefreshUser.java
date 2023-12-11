package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
public class RefreshUser {

    private String userIdentifier;
    private LocalDateTime lastUpdated;
    private String organisationIdentifier;
    private Set<AccessType> accessTypes;

    public RefreshUser(String userIdentifier,
                       LocalDateTime lastUpdated,
                       String organisationIdentifier,
                       Set<AccessType> accessTypes) {
        this.userIdentifier = userIdentifier;
        this.lastUpdated = lastUpdated;
        this.organisationIdentifier = organisationIdentifier;
        this.accessTypes = accessTypes;
    }
}
