package uk.gov.hmcts.reform.professionalapi.controller.request;

import lombok.Getter;
import uk.gov.hmcts.reform.professionalapi.controller.response.ContactInformationValidationResponse;

import java.io.Serializable;
import java.util.List;

@Getter
public class InvalidContactInformations  extends RuntimeException implements Serializable {
    private static final long serialVersionUID = -3909103963189618433L;
    List<ContactInformationValidationResponse> contactInfoValidations;

    public InvalidContactInformations(
            String message, List<ContactInformationValidationResponse> contactInfoValidations) {
        super(message);
        this.contactInfoValidations = contactInfoValidations;
    }
}
