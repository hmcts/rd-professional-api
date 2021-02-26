package uk.gov.hmcts.reform.professionalapi.controller.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationMfaStatus;

@Setter
@Getter
@NoArgsConstructor
public class MfaStatusResponse {

    @JsonProperty
    private String mfa;

    public MfaStatusResponse(OrganisationMfaStatus mfaStatus) {
        this.mfa = mfaStatus.getMfaStatus().name();
    }

}
