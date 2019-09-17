package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserRolesResponse {

    private int statusCode;
    private String statusMessage;

    public UserRolesResponse(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

}

