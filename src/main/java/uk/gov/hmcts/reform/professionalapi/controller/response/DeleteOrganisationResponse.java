package uk.gov.hmcts.reform.professionalapi.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DeleteOrganisationResponse {

    private int statusCode;
    private String message;

    public DeleteOrganisationResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
}