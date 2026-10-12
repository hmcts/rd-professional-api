package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateOrgSraResponse {

    @JsonProperty
    private String organisationId;
    @JsonProperty
    private String status;
    @JsonProperty
    private int statusCode;
    @JsonProperty
    private String message;

}