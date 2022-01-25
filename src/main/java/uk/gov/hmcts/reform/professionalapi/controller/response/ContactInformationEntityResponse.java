package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactInformationEntityResponse {


    @JsonInclude(NON_NULL)
    @JsonProperty("organisationIdentifier")
    private String organisationIdentifier;


    @JsonInclude(NON_NULL)
    @JsonProperty("contactInfoValidations")

    private List<ContactInformationValidationResponse> contactInfoValidations;

}
