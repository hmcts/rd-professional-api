package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationIdentifierValidator;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.COMMA;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.REG_EXP_COMMA_DILIMETER;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.*;

@Component
@Slf4j
public class OrganisationStatusValidatorImpl implements OrganisationIdentifierValidator {

    @Value("${loggingComponentName}")
    protected String loggingComponentName;

    @Override
    public void validate(Organisation existingOrganisation, OrganisationStatus inputStatus,
                         String inputOrganisationIdentifier) {
        //Into Organisation status validator

        validateOrganisationStatus(inputStatus, existingOrganisation.getStatus(), inputOrganisationIdentifier);

        //Validation completed for Update Organisation
    }

    private void validateOrganisationStatus(OrganisationStatus inputRequestOrganisationStatus,
                                            OrganisationStatus existingStatus, String inputOrganisationIdentifier) {
        if (existingStatus == DELETED) {
            String errorMessage = "{}:: Cannot amend status since existing organisation status is DELETED for "
                    .concat("organisationIdentifier: ") + inputOrganisationIdentifier;
            log.error(errorMessage, loggingComponentName);
            throw new InvalidRequest(errorMessage);
        } else if (inputRequestOrganisationStatus == PENDING
                && existingStatus == ACTIVE) {
            String errorMessage = "{}:: Cannot amend status to PENDING since existing organisation"
                    .concat(" status is ACTIVE for organisationIdentifier: ") + inputOrganisationIdentifier;
            log.error(errorMessage, loggingComponentName);
            throw new InvalidRequest(errorMessage);
        }
    }

    public static List<String> checkIfValidOrgStatusesAndReturnList(String statuses, String exceptionMessage) {
        checkIfStringStartsAndEndsWithComma(statuses, exceptionMessage);
        var statusList = new ArrayList<>(Arrays.asList(statuses.split(REG_EXP_COMMA_DILIMETER)));
        statusList.replaceAll(String::trim);
        if (isEmpty(statusList) || isEmpty(doesListContainInvalidStatusIgnoreCase(statusList))) {
            throw new InvalidRequest(String.format(exceptionMessage, statuses));
        }
        return statusList;
    }

    private static void checkIfStringStartsAndEndsWithComma(String statuses, String exceptionMessage) {
        if (StringUtils.startsWith(statuses, COMMA) || StringUtils.endsWith(statuses, COMMA)) {
            throw new InvalidRequest(String.format(exceptionMessage, statuses));
        }
    }

    public static boolean doesListContainInvalidStatusIgnoreCase(List<String> statusList) {
        return isEmpty(statusList.stream()
                .filter(s -> !s.equalsIgnoreCase(PENDING.name())
                        && !s.equalsIgnoreCase(ACTIVE.name())
                        && !s.equalsIgnoreCase(BLOCKED.name())
                        && !s.equalsIgnoreCase(DELETED.name()))
                .count());
    }
}
