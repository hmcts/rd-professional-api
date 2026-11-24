package uk.gov.hmcts.reform.professionalapi.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
public class MultipleOrganisationsResponse {

    private List<OrganisationByProfileResponse> organisationInfo;
    private UUID lastRecordInPage;
    private boolean moreAvailable;

    public MultipleOrganisationsResponse(List<Organisation> organisations, boolean moreAvailable) {
        this.organisationInfo = organisations.stream().map(OrganisationByProfileResponse::new).toList();

        this.lastRecordInPage = Optional.of(organisations)
                .filter(organisation -> !organisation.isEmpty())
                .map(organisation -> organisation.get(organisation.size() - 1).getId())
                .orElse(null);

        this.moreAvailable = moreAvailable;
    }
}
