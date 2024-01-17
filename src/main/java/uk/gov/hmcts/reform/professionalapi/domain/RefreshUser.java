package uk.gov.hmcts.reform.professionalapi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private OrganisationInfo organisationInfo;
    private List<UserAccessType> userAccessTypes;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime dateTimeDeleted;
    private long recordNumber;

    public RefreshUser(String userIdentifier,
                       LocalDateTime lastUpdated,
                       OrganisationInfo organisationInfo,
                       List<UserAccessType> userAccessTypes,
                       LocalDateTime dateTimeDeleted,
                       long recordNumber) {
        this.userIdentifier = userIdentifier;
        this.lastUpdated = lastUpdated;
        this.organisationInfo = organisationInfo;
        this.userAccessTypes = userAccessTypes;
        this.dateTimeDeleted = dateTimeDeleted;
        this.recordNumber = recordNumber;
    }
}
