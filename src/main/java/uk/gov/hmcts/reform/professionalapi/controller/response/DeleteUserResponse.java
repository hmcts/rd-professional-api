package uk.gov.hmcts.reform.professionalapi.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DeleteUserResponse {

    private int statusCode;
    private String message;

}