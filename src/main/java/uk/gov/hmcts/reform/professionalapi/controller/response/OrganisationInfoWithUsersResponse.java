package uk.gov.hmcts.reform.professionalapi.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
public class OrganisationInfoWithUsersResponse {
    private String organisationIdentifier;
    private String organisationStatus;
    private List<String> organisationProfileIds;
    private List<ProfessionalUsersResponseWithoutRoles> users;
}
