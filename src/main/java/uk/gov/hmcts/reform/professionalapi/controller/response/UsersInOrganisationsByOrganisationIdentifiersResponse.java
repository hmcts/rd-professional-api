package uk.gov.hmcts.reform.professionalapi.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
public class UsersInOrganisationsByOrganisationIdentifiersResponse {
    private List<OrganisationInfoWithUsersResponse> organisationInfo;
    private UUID lastOrgInPage;
    private UUID lastUserInPage;
    private boolean moreAvailable;

    public UsersInOrganisationsByOrganisationIdentifiersResponse(List<ProfessionalUser> professionalUsers,
                                                                 boolean moreAvailable) {
        this.moreAvailable = moreAvailable;
        this.organisationInfo = mapToOrganisationInfo(professionalUsers);
    }

    private List<OrganisationInfoWithUsersResponse> mapToOrganisationInfo(List<ProfessionalUser> professionalUsers) {
        Map<String, OrganisationInfoWithUsersResponse> organisationInfoMap = new HashMap<>();

        for (ProfessionalUser professionalUser : professionalUsers) {
            // if the organisationInfoMap does not contain the organisationIdentifier as a key, then add it
            String organisationIdentifier = professionalUser.getOrganisation().getOrganisationIdentifier();
            OrganisationInfoWithUsersResponse organisationInfo = organisationInfoMap.get(organisationIdentifier);
            if (organisationInfo == null) {
                organisationInfo = new OrganisationInfoWithUsersResponse(professionalUser.getOrganisation());
                organisationInfoMap.put(organisationIdentifier, organisationInfo);
            }

            organisationInfo.AddUser(professionalUser);

            lastOrgInPage = professionalUser.getOrganisation().getId();
            lastUserInPage = professionalUser.getId();
        }

        return new ArrayList<>(organisationInfoMap.values());
    }
}


