package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.response;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import uk.gov.hmcts.reform.professionalapi.domain.entities.Organisation;

public class OrganisationResponse {

    @JsonProperty
    private final String id;
    @JsonProperty
    private final String name;
    @JsonProperty
    private final List<String> userIds;
    @JsonProperty
    private final List<String> pbaAccounts;

    public OrganisationResponse(Organisation organisation) {
        this.id = organisation.getId().toString();
        this.name = organisation.getName();
        this.userIds = organisation.getUsers()
                .stream()
                .map(user -> user.getId().toString())
                .collect(toList());
        this.pbaAccounts = organisation.getPaymentAccounts()
                .stream()
                .map(acc -> acc.getPbaNumber())
                .collect(toList());
    }
}
