package uk.gov.hmcts.reform.professionalapi.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class OrganisationInfoWithUsersResponse {
    private String organisationIdentifier;
    private String status;
    private List<String> organisationProfileIds;
    private List<OrganisationUserResponse> users;

    public OrganisationInfoWithUsersResponse(Organisation organisation, List<ProfessionalUser> professionalUsers) {
        this.users = new ArrayList<>();
        this.organisationIdentifier = organisation.getOrganisationIdentifier();
        this.status = organisation.getStatus().name();
        this.organisationProfileIds = RefDataUtil.getOrganisationProfileIds(organisation);

        professionalUsers.forEach(user -> {
            OrganisationUserResponse userResponse = new OrganisationUserResponse(user);
            users.add(userResponse);
        });
    }
}

