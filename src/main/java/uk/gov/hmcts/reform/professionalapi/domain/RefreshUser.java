package uk.gov.hmcts.reform.professionalapi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class RefreshUser {

    private String userIdentifier;
    private LocalDateTime lastUpdated;
    private OrganisationInfo organisationInfo;
    private List<UserAccessType> userAccessTypes;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime dateTimeDeleted;
}
