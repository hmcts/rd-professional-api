package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

@Component
public class OrganisationByProfileIdsRequestValidator {
    public void validate(Integer pageSize) {
        if (pageSize != null && pageSize < 1) {
            throw new InvalidRequest("001 missing/invalid pageSize");
        }
    }
}
