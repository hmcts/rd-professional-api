package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@JsonPropertyOrder({"message"})
public class UpdatePbaStatusResponse {

    @JsonInclude(NON_NULL)
    @JsonProperty("message")
    String partialSuccessMessage;

    @JsonInclude(NON_NULL)
    @JsonProperty
    List<PbaUpdateStatusResponse> pbaUpdateStatusResponses;

    @JsonIgnore
    int statusCode;

    public UpdatePbaStatusResponse(
            String partialSuccessMessage, List<PbaUpdateStatusResponse> pbaUpdateStatusResponses, int statusCode) {
        this.partialSuccessMessage = partialSuccessMessage;
        this.pbaUpdateStatusResponses = pbaUpdateStatusResponses;
        this.statusCode = statusCode;
    }

}
