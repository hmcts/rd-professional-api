package uk.gov.hmcts.reform.professionalapi.controller.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrganisationByProfileIdsRequest {
    private List<String> organisationProfileIds;
}
