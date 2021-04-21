package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.MFAStatus;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class MfaUpdateRequest {

    @NotNull
    private MFAStatus mfa;

    @JsonCreator
    public MfaUpdateRequest(@JsonProperty MFAStatus mfa) {
        this.mfa = mfa;
    }

}
