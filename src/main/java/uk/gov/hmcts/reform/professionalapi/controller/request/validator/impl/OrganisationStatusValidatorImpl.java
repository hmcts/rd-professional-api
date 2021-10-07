package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import com.sun.tools.rngom.digested.DZeroOrMorePattern;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.codehaus.groovy.runtime.InvokerHelper.escapeBackslashes;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.COMMA;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.EXCEPTION_MSG_NO_VALID_ORG_STATUS_PASSED;
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
        if (existingStatus.isDeleted()) {
            String errorMessage = "{}:: Cannot amend status since existing organisation status is DELETED for "
                    .concat("organisationIdentifier: ") + inputOrganisationIdentifier;
            log.error(errorMessage, loggingComponentName);
            throw new InvalidRequest(errorMessage);
        } else if ((inputRequestOrganisationStatus.isPending()
                || inputRequestOrganisationStatus.isReview())
                && existingStatus.isActive()) {
            String errorMessage = "{}:: Cannot amend status to PENDING/REVIEW since existing organisation"
                    .concat(" status is ACTIVE for organisationIdentifier: ") + inputOrganisationIdentifier;
            log.error(errorMessage, loggingComponentName);
            throw new InvalidRequest(errorMessage);
        }
    }

    @SuppressWarnings("unchecked")
    public static List<String> validateAndReturnStatusList(String statuses) {
        List<String> statusList = null;
        if (isBlank(statuses)) {
            throwInvalidOrgStatus("null or empty");
        } else {
            //ignore leading and trailing commas by removing them
            statuses = statuses.trim().replaceAll(",$|^,", "");
            // get all statuses mentioned in
            statusList = asList(statuses.split(REG_EXP_COMMA_DILIMETER));
            statusList.replaceAll(String::trim);
            if (areInvalidValidOrgStatuses(statusList)) {
                throwInvalidOrgStatus(statuses);
            }
        }
        statusList.replaceAll(String::toUpperCase);
        return statusList;
    }

    public static List<String> throwInvalidOrgStatus(String statuses) {
        throw new InvalidRequest(String.format(EXCEPTION_MSG_NO_VALID_ORG_STATUS_PASSED, statuses));
    }

    public static boolean areInvalidValidOrgStatuses(List<String> statusList) {
        List<String> validOrgStatuses = Stream.of(OrganisationStatus.values()).map(Enum::name).collect(toList());
        return statusList.stream().anyMatch(s -> (s.isBlank() || !validOrgStatuses.contains(s.toUpperCase())));
    }

    public static boolean doesListContainActiveStatus(List<String> statuses) {
        return statuses.stream().anyMatch(status -> status.equalsIgnoreCase(ACTIVE.name()));
    }

    public static List<OrganisationStatus> getOrgStatusEnumsExcludingActiveStatus(List<String> statuses) {
        statuses.remove(ACTIVE.name());
        return statuses.stream().map(OrganisationStatus::valueOf).collect(toList());
    }
}
