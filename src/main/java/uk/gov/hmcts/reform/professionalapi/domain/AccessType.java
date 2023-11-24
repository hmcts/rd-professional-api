package uk.gov.hmcts.reform.professionalapi.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
public class AccessType {

      private String jurisdictionId;
      private String organisationProfileId;
      private String accessTypeId;
      private Boolean enabled;

    public AccessType(@JsonProperty(value = "jurisdictionId") String jurisdictionId,
                      @JsonProperty(value = "organisationProfileId") String organisationProfileId,
                      @JsonProperty(value = "accessTypeId") String accessTypeId,
                      @JsonProperty(value = "enabled") Boolean enabled) {
        this.jurisdictionId = jurisdictionId;
        this.organisationProfileId = organisationProfileId;
        this.accessTypeId = accessTypeId;
        this.enabled = enabled;
    }

}
