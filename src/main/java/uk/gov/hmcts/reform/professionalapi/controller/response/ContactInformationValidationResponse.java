package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.controller.request.ContactInformationCreationRequest;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "aContactInformationValidationResponse")
public class ContactInformationValidationResponse {
    @JsonProperty("uprn")
    private String uprn;
    @JsonProperty("address_valid")
    private boolean validAddress;
    @JsonInclude(NON_NULL)
    @JsonProperty("error_description")
    private String errorDescription;
}
