package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class AccessType {

    private String jurisdictionId;
    private String organisationProfileId;
    private String accessTypeId;
    private boolean enabled;

    public AccessType(String jurisdictionId, String organisationProfileId, String accessTypeId, boolean enabled) {
        this.jurisdictionId = jurisdictionId;
        this.organisationProfileId = organisationProfileId;
        this.accessTypeId = accessTypeId;
        this.enabled = enabled;
    }
}
