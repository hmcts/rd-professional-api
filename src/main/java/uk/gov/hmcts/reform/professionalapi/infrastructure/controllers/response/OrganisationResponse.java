package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.professionalapi.domain.entities.Organisation;

public class OrganisationResponse {

    @JsonProperty
    private final String id;
    @JsonProperty
    private final String name;
    @JsonProperty
    private final List<String> userIds;

    public OrganisationResponse(Organisation organisation) {
        this.id = organisation.getId();
        this.name = organisation.getName();
        this.userIds = organisation.getUsers()
                .stream()
                .map(user -> user.getId())
                .collect(Collectors.toList());
    }
}
