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
    private String organisationStatus;
    private List<String> organisationProfileIds;
    private List<OrganisationUserResponse> users;

    public OrganisationInfoWithUsersResponse(Organisation organisation) {
        this.users = new ArrayList<>();
        this.organisationIdentifier = organisation.getOrganisationIdentifier();
        this.organisationStatus = organisation.getStatus().name();
        this.organisationProfileIds = RefDataUtil.getOrganisationProfileIds(organisation);
    }

    public void AddUser(ProfessionalUser professionalUser) {
        OrganisationUserResponse userResponse = new OrganisationUserResponse(professionalUser);
        this.users.add(userResponse);
    }
}

