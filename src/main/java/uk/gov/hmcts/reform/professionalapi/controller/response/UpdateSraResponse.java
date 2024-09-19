package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@JsonPropertyOrder({"status"})
@AllArgsConstructor
public class UpdateSraResponse {

    @JsonInclude(NON_NULL)
    @JsonProperty("status")
    private String status;

    @JsonInclude(NON_NULL)
    @JsonProperty("message")
    private String message;

    @JsonInclude(NON_NULL)
    @JsonProperty(value = "names")
    private List<UpdateOrgSraResponse> updateOrgSraResponse;

}