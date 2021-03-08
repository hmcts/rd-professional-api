package uk.gov.hmcts.reform.professionalapi.controller.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class MfaStatusResponse {

    @JsonProperty
    private String mfa;

}
