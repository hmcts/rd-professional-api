package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.AccessType;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserConfiguredAccess;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class ProfessionalUsersResponseWithoutRoles {

    @JsonProperty
    public String userIdentifier;
    @JsonProperty
    public String firstName;
    @JsonProperty
    public String lastName;
    @JsonProperty
    public String email;
    @JsonProperty
    public String idamStatus;
    @JsonProperty
    public List<AccessType> accessTypes;

    public ProfessionalUsersResponseWithoutRoles(ProfessionalUser user) {
        this.userIdentifier = user.getUserIdentifier();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmailAddress();
        this.idamStatus = user.getIdamStatus() == null ? "" : user.getIdamStatus().toString();
        this.accessTypes = user.getUserConfiguredAccesses().stream().map(uca -> fromUserConfiguredAccess(uca))
                .collect(Collectors.toList());
    }

    public AccessType fromUserConfiguredAccess(UserConfiguredAccess userConfiguredAccess) {
        AccessType accessType = new AccessType();
        accessType.setAccessTypeId(userConfiguredAccess.getUserConfiguredAccessId().getAccessTypeId());
        accessType.setOrganisationProfileId(userConfiguredAccess.getUserConfiguredAccessId().getOrganisationProfileId());
        accessType.setJurisdictionId(userConfiguredAccess.getUserConfiguredAccessId().getJurisdictionId());
        accessType.setEnabled(userConfiguredAccess.getEnabled());

        return accessType;
    }
}
