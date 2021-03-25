package uk.gov.hmcts.reform.professionalapi.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.professionalapi.domain.MFAStatus;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
public class MfaUpdateRequest {

    @NotEmpty
    private MFAStatus mfaStatus;

    public MfaUpdateRequest(@JsonProperty MFAStatus mfaStatus) {
        this.mfaStatus = mfaStatus;
    }

}
