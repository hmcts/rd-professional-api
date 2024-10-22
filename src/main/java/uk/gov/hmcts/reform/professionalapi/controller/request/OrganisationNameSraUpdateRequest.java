package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder(builderMethodName = "anOrganisationCreationRequest")
public class OrganisationNameSraUpdateRequest {

    @NotNull
    private String name;
    private  String sraId;

    @JsonCreator
    public OrganisationNameSraUpdateRequest(
            @JsonProperty("name") String name,
            @JsonProperty("sraId") String sraId) {
        this.name = name;
        this.sraId = sraId;
    }
}