package uk.gov.hmcts.reform.professionalapi.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

import java.util.ArrayList;
import java.util.List;
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
        if (!this.organisationInfo.isEmpty()) {
            this.lastOrgInPage = professionalUsers.get(professionalUsers.size() - 1).getOrganisation().getId();
            this.lastUserInPage = professionalUsers.get(professionalUsers.size() - 1).getId();
        }
    }

    private List<OrganisationInfoWithUsersResponse> mapToOrganisationInfo(List<ProfessionalUser> professionalUsers) {
        // important to maintain the order of organisations and the users within them
        List<Organisation> organisations = new ArrayList<>(professionalUsers.stream()
                .map(ProfessionalUser::getOrganisation)
                .distinct().toList());

        List<OrganisationInfoWithUsersResponse> sortedOrganisationInfo = new ArrayList<>();

        for (Organisation organisation : organisations) {
            List<ProfessionalUser> users = new ArrayList<>();
            for (ProfessionalUser professionalUser : professionalUsers) {
                if (professionalUser.getOrganisation().getId().equals(organisation.getId())) {
                    users.add(professionalUser);
                }
            }
            sortedOrganisationInfo.add(new OrganisationInfoWithUsersResponse(organisation, users));
        }

        return sortedOrganisationInfo;
    }
}


