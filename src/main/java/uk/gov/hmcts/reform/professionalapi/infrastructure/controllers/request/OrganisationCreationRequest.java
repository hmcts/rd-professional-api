package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderMethodName = "anOrganisationCreationRequest")
public class OrganisationCreationRequest {

    @NotNull
    private final String name;

    @NotNull
    private final UserCreationRequest superUser;

    private List<PbaAccountCreationRequest> pbaAccounts;

    @JsonCreator
    public OrganisationCreationRequest(
            @JsonProperty("name") String name,
            @JsonProperty("superUser") UserCreationRequest superUser,
            @JsonProperty("pbaAccounts") List<PbaAccountCreationRequest> pbaAccountCreationRequests) {

        this.name = name;
        this.superUser = superUser;
        this.pbaAccounts = pbaAccountCreationRequests;
    }



}

