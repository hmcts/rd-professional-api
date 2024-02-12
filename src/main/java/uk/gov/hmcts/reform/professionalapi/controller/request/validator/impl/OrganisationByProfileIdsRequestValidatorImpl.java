package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

import java.util.UUID;

@Component
public class OrganisationByProfileIdsRequestValidatorImpl {
    public void validate(Integer pageSize) {
        if (pageSize != null && pageSize < 1) {
            throw new InvalidRequest("Invalid pageSize");
        }
    }
}
