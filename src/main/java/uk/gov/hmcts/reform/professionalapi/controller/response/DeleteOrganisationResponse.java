package uk.gov.hmcts.reform.professionalapi.controller.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteOrganisationResponse {

    private int statusCode;
    private String message;

}