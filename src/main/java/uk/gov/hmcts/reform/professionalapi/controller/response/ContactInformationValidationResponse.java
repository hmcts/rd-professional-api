package uk.gov.hmcts.reform.professionalapi.controller.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@NoArgsConstructor
public class ContactInformationValidationResponse implements Serializable {
    private static final long serialVersionUID = -3651459804052221811L;
    @JsonProperty("uprn")
    private String uprn;
    @JsonProperty("address_valid")
    private boolean validAddress;
    @JsonInclude(NON_NULL)
    @JsonProperty("error_description")
    private String errorDescription;
}
