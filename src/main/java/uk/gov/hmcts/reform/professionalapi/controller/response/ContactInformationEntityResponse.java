package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Setter
@Getter
@NoArgsConstructor
@JsonPropertyOrder({"message"})
public class ContactInformationEntityResponse {
    @JsonInclude(NON_NULL)
    @JsonProperty("message")
    String partialSuccessMessage;

    @JsonInclude(NON_NULL)
    @JsonProperty
    List<ContactInformationResponseWithDxAddress> contactInformationsResponse;

    @JsonIgnore
    int statusCode;
}
