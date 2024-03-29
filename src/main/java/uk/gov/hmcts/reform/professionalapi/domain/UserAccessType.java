package uk.gov.hmcts.reform.professionalapi.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class UserAccessType {

    private String jurisdictionId;
    private String organisationProfileId;
    private String accessTypeId;
    private Boolean enabled;

    public UserAccessType(@JsonProperty("jurisdictionId") String jurisdictionId,
                          @JsonProperty("organisationProfileId") String organisationProfileId,
                          @JsonProperty("accessTypeId") String accessTypeId,
                          @JsonProperty("enabled") Boolean enabled) {
        this.jurisdictionId = jurisdictionId;
        this.organisationProfileId = organisationProfileId;
        this.accessTypeId = accessTypeId;
        this.enabled = enabled;
    }

}
