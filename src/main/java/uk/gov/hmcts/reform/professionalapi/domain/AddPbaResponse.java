package uk.gov.hmcts.reform.professionalapi.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddPbaResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private FailedPbaReason reason;

    public AddPbaResponse(String message, FailedPbaReason reason) {
        this.message = message;
        this.reason = reason;
    }
}
