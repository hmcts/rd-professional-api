package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class OrganisationCreationRequest {

    @NotNull
    private final String name;
    @NotNull
    private final UserCreationRequest superUser;

    @JsonCreator
    public OrganisationCreationRequest(
            @JsonProperty("name") String name,
            @JsonProperty("superUser") UserCreationRequest superUser) {

        this.name = name;
        this.superUser = superUser;
    }
}

