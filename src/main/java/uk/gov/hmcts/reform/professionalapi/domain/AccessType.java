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
}
