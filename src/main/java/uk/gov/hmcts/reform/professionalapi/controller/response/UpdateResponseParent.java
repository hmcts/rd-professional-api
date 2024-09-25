package uk.gov.hmcts.reform.professionalapi.controller.response;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"status"})
@AllArgsConstructor
public class UpdateResponseParent {

    @JsonInclude(NON_NULL)
    @JsonProperty("status")
    private String status;

    @JsonInclude(NON_NULL)
    @JsonProperty("message")
    private String message;

    @JsonInclude(NON_NULL)
    @JsonProperty(value = "responses")
    private List<UpdateOrgResponse> updateOrgResponse;

}
