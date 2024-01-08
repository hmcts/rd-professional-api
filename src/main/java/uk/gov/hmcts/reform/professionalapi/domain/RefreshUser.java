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
    private String organisationIdentifier;
    private OrganisationStatus organisationStatus;
    private List<String> organisationProfileIds;
    private List<UserAccessType> userAccessTypes;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime dateTimeDeleted;

    public RefreshUser(String userIdentifier,
                       LocalDateTime lastUpdated,
                       String organisationIdentifier,
                       OrganisationStatus organisationStatus,
                       List<String> organisationProfileIds,
                       List<UserAccessType> userAccessTypes,
                       LocalDateTime dateTimeDeleted) {
        this.userIdentifier = userIdentifier;
        this.lastUpdated = lastUpdated;
        this.organisationIdentifier = organisationIdentifier;
        this.organisationStatus = organisationStatus;
        this.organisationProfileIds = organisationProfileIds;
        this.userAccessTypes = userAccessTypes;
        this.dateTimeDeleted = dateTimeDeleted;
    }
}
