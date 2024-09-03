package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder(builderMethodName = "anOrganisationCreationRequest")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrganisationNameUpdateRequest {

    @Valid
    @NotNull(message = "Name is required.")
    private String name;

    @JsonCreator
    public OrganisationNameUpdateRequest(
            @JsonProperty("name") String name) {
        this.name = name;
    }
}