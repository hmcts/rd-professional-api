package uk.gov.hmcts.reform.professionalapi.controller.request.validator;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class UpdateOrganisationRequestValidator {

    private final List<OrganisationIdentifierValidator> validators;

    public void validateStatus(Organisation existingOrganisation, OrganisationStatus inputStatus,
                               String inputOrganisationIdentifier) {
        validators.forEach(v -> v.validate(existingOrganisation, inputStatus, inputOrganisationIdentifier));
    }
}