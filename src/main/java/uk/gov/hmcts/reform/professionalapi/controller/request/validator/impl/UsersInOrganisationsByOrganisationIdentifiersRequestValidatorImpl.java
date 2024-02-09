package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

import java.util.UUID;

@Component
public class UsersInOrganisationsByOrganisationIdentifiersRequestValidatorImpl {
    public void validate(Integer pageSize, UUID searchAfterOrg, UUID searchAfterUser) {
        if (pageSize != null && pageSize < 1) {
            throw new InvalidRequest("Invalid pageSize");
        }
        if (searchAfterOrg != null && searchAfterOrg.toString().equals("00000000-0000-0000-0000-000000000000")) {
            throw new InvalidRequest("Invalid searchAfterOrg");
        }
        if (searchAfterUser != null && searchAfterUser.toString().equals("00000000-0000-0000-0000-000000000000")) {
            throw new InvalidRequest("Invalid searchAfterUser");
        }

        if (searchAfterUser != null && searchAfterOrg == null) {
            throw new InvalidRequest("searchAfterOrg cannot be null when searchAfterUser is provided");
        }

        if (searchAfterOrg != null && searchAfterUser == null) {
            throw new InvalidRequest("searchAfterOrg cannot be null when searchAfterUser is provided");
        }

        if (searchAfterOrg != null && searchAfterUser != null && pageSize == null) {
            throw new InvalidRequest("pageSize cannot be null when searchAfterOrg and searchAfterUser are provided");
        }
    }
}