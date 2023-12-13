package uk.gov.hmcts.reform.professionalapi.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    public static AccessType fromUserConfiguredAccess(UserConfiguredAccess userConfiguredAccess) {
        AccessType accessType = new AccessType();
        accessType.setAccessTypeId(userConfiguredAccess.getUserConfiguredAccessId().getAccessTypeId());
        accessType.setOrganisationProfileId(userConfiguredAccess.getUserConfiguredAccessId()
                .getOrganisationProfileId());
        accessType.setJurisdictionId(userConfiguredAccess.getUserConfiguredAccessId().getJurisdictionId());
        accessType.setEnabled(userConfiguredAccess.getEnabled());

        return accessType;
    }

}
