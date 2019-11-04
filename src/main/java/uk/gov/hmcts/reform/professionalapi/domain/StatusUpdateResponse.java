package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StatusUpdateResponse {

    private String idamStatusCode;
    private String idamMessage;

    public StatusUpdateResponse(String idamStatusCode, String idamMessage) {

        this.idamStatusCode = idamStatusCode;
        this.idamMessage = idamMessage;
    }

}
