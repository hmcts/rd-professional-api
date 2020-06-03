package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class OrganisationMinimalInfoResponse {
    @JsonProperty
    protected String organisationIdentifier;
    @JsonProperty
    protected String name;
}