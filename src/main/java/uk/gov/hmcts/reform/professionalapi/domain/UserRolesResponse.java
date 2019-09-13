package uk.gov.hmcts.reform.professionalapi.domain;

import lombok.Getter;

@Getter
public class UserRolesResponse {

    private String statusCode;
    private String statusMessage;

    public UserRolesResponse(String statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

}

