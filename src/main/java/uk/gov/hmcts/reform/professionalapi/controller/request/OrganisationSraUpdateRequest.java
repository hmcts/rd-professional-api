package uk.gov.hmcts.reform.professionalapi.controller.request;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "anOrganisationCreationRequest")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisationSraUpdateRequest {

    @NotNull
    @NotNull(message = "SraId is required.")
    private  String sraId;

    @JsonCreator
    public OrganisationSraUpdateRequest(
            @JsonProperty("sraId") String sraId) {
        this.sraId = sraId;
    }
}