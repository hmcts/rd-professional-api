package uk.gov.hmcts.reform.professionalapi.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

import java.util.ArrayList;
import java.util.Comparator;
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
        // get all distinct organisations from the list of professionalUsers and sort them
        List<Organisation> organisations = new ArrayList<>(professionalUsers.stream()
                .map(ProfessionalUser::getOrganisation)
                .distinct().toList());

        organisations.sort(Comparator.comparing(org -> org.getId().toString()));
        List<OrganisationInfoWithUsersResponse> sortedOrganisationInfo = new ArrayList<>();

        for (Organisation organisation : organisations) {
            // find the professionalUsers that belong to the organisation and sort them
            List<ProfessionalUser> users = new ArrayList<>();
            for (ProfessionalUser professionalUser : professionalUsers) {
                if (professionalUser.getOrganisation().getId().equals(organisation.getId())) {
                    users.add(professionalUser);
                }
            }
            users.sort(Comparator.comparing(user -> user.getId().toString()));
            sortedOrganisationInfo.add(new OrganisationInfoWithUsersResponse(organisation, users));
        }

        return sortedOrganisationInfo;
    }
}


