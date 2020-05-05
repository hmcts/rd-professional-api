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
    private String errorDescription;

    public DeleteOrganisationResponse(int statusCode, String message, String errorDescription) {
        this.statusCode = statusCode;
        this.message = message;
        this.errorDescription = errorDescription;

    }
}