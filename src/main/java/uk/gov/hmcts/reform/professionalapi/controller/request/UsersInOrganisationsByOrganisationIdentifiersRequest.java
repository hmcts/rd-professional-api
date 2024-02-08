package uk.gov.hmcts.reform.professionalapi.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UsersInOrganisationsByOrganisationIdentifiersRequest {
    private List<String> organisationIdentifiers;
}
