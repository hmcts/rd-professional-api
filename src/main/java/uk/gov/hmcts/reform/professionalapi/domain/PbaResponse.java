package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PbaResponse {

    private String statusCode;
    private String statusMessage;

    public PbaResponse(String statusCode, String statusMessage) {

        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

}
