package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddPbaResponse {

    private String message;
    private FailedPbaReason reason;

    public AddPbaResponse(String message, FailedPbaReason reason) {
        this.message = message;
        this.reason = reason;
    }
}
