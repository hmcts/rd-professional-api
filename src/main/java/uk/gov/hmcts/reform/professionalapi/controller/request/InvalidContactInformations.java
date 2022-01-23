package uk.gov.hmcts.reform.professionalapi.controller.request;

import lombok.Getter;
import uk.gov.hmcts.reform.professionalapi.controller.response.ContactInformationValidationResponse;

import java.util.List;
@Getter
public class InvalidContactInformations extends RuntimeException {
    List<ContactInformationValidationResponse> contactInfoValidations;
    public InvalidContactInformations(String message, List<ContactInformationValidationResponse> contactInfoValidations) {
        super(message);
        this.contactInfoValidations = contactInfoValidations;
    }
}
