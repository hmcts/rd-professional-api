package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@JsonPropertyOrder({"message"})
@AllArgsConstructor
public class UpdatePbaStatusResponse {

    @JsonInclude(NON_NULL)
    @JsonProperty("message")
    private String partialSuccessMessage;

    @JsonInclude(NON_NULL)
    @JsonProperty
    private List<PbaUpdateStatusResponse> pbaUpdateStatusResponses;

    @JsonIgnore
    private int statusCode;

}
