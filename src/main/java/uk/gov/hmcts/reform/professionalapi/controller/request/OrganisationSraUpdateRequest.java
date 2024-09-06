package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@Getter
@Setter
@Builder(builderMethodName = "anOrganisationSraUpdateRequest")
public class OrganisationSraUpdateRequest {

    @Valid
    @NotNull(message = "SRA Id is required.")
    private  String sraId;

    @JsonCreator
    public OrganisationSraUpdateRequest(
            @JsonProperty("sraId") String sraId) {
        this.sraId = sraId;
    }
}