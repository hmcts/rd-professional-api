package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
//@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"message"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactInformationEntityResponse {

    @JsonInclude(NON_NULL)
    @JsonProperty("message")
    private String partialSuccessMessage;

    @JsonInclude(NON_NULL)
    @JsonProperty("organisationIdentifier")
    private  String organisationIdentifier;


    @JsonInclude(NON_NULL)
    @JsonProperty("contactInformationsResponse")
    private List<ContactInformationResponseWithDxAddress> contactInformationsResponse;

    @JsonInclude(NON_NULL)
    @JsonProperty("contactInfoValidations")

    private List<ContactInformationValidationResponse> contactInfoValidations;

    @JsonIgnore
    @JsonProperty("statusCode")
    private int statusCode;

}
