package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class OrganisationMinimalInfoResponse {
    @JsonProperty
    protected String name;
    @JsonProperty
    protected String organisationIdentifier;
}