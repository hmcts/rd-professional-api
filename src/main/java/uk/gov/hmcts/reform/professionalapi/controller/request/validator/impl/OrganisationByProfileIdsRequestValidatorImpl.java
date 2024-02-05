package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

import java.util.UUID;

@Component
public class OrganisationByProfileIdsRequestValidatorImpl {
    public void validate(Integer pageSize, UUID searchAfter) {
        if (pageSize != null && pageSize < 1) {
            throw new InvalidRequest("Invalid pageSize");
        }
        if (searchAfter != null && (searchAfter.toString().isEmpty() || isDefaultUuid(searchAfter))) {
            throw new InvalidRequest("Invalid searchAfter");
        }
    }

    private boolean isDefaultUuid(UUID uuid) {
        return uuid.toString().equals("00000000-0000-0000-0000-000000000000");
    }
}
