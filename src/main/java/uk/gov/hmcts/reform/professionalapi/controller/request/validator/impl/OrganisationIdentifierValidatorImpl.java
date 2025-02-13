package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.controller.advice.FieldAndPersistenceValidationException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UpdateContactInformationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationIdentifierValidator;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MESSAGE_403_FORBIDDEN;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.INVALID_MANDATORY_PARAMETER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.INVALID_PAGE_INFORMATION;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.INVALID_SINCE_TIMESTAMP;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ISO_DATE_TIME_FORMATTER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LAST_UPDATED_SINCE_TIMESTAMP_FORMAT;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.LOG_TWO_ARG_PLACEHOLDER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.NO_ORG_FOUND_FOR_GIVEN_ID;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ORG_NOT_ACTIVE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.SINCE_TIMESTAMP_FORMAT;

@Component
@Slf4j
public class OrganisationIdentifierValidatorImpl implements OrganisationIdentifierValidator {

    private final OrganisationService organisationService;

    @Value("${loggingComponentName}")
    protected String loggingComponentName;

    @Autowired
    public OrganisationIdentifierValidatorImpl(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    @Override
    public void validate(Organisation existingOrganisation, OrganisationStatus inputStatus,
                         String inputOrganisationIdentifier) {
        //Into Organisation identifier validator
        checkOrganisationDoesNotExist(existingOrganisation, inputOrganisationIdentifier);
        //Validation completed for identifier Organisation
    }

    private void checkOrganisationDoesNotExist(Organisation organisation, String inputOrganisationIdentifier) {
        if (null == organisation) {
            var errorMessage = NO_ORG_FOUND_FOR_GIVEN_ID + inputOrganisationIdentifier;
            log.error(loggingComponentName, errorMessage);
            throw new EmptyResultDataAccessException(errorMessage, 1);
        }
    }

    public void verifyExtUserOrgIdentifier(Organisation organisation, String extOrgIdentifier) {

        if (null == organisation || organisation.getPaymentAccounts().isEmpty()) {
            throw new EmptyResultDataAccessException(1);

        } else if (!extOrgIdentifier.trim().equals(organisation.getOrganisationIdentifier().trim())) {
            throw new AccessDeniedException(ERROR_MESSAGE_403_FORBIDDEN);
        }
    }

    public void verifyNonPuiFinanceManagerOrgIdentifier(List<String> authorities, Organisation organisation,
                                                        String extOrgIdentifier) {

        var isPuiFinanceManExist = ifUserRoleExists(authorities, "pui-finance-manager");

        if (!isPuiFinanceManExist) {
            authorities.forEach(role -> RefDataUtil.validateOrgIdentifier(extOrgIdentifier,
                    organisation.getOrganisationIdentifier()));
        }
    }

    public boolean ifUserRoleExists(List<String> roles, String role) {
        boolean doesRoleExist = false;
        for (String roleName : roles) {

            if (isNotBlank(roleName) && role.equals(roleName.trim())) {
                doesRoleExist = true;
                break;
            }
        }
        return doesRoleExist;
    }

    public void validateOrganisationIsActive(Organisation existingOrganisation, HttpStatus statusCodeToBeThrown) {
        if (OrganisationStatus.ACTIVE != existingOrganisation.getStatus()) {

            if (statusCodeToBeThrown == NOT_FOUND) {
                log.error(LOG_TWO_ARG_PLACEHOLDER, loggingComponentName, ORG_NOT_ACTIVE);
                throw new EmptyResultDataAccessException(1);
            } else if (statusCodeToBeThrown == BAD_REQUEST) {
                log.error(LOG_TWO_ARG_PLACEHOLDER, loggingComponentName, ORG_NOT_ACTIVE);
                throw new InvalidRequest(ORG_NOT_ACTIVE);
            }
        }
    }

    public void validateOrganisationExistsWithGivenOrgId(String orgId) {
        if (null == organisationService.getOrganisationByOrgIdentifier(orgId)) {
            log.error(LOG_TWO_ARG_PLACEHOLDER, loggingComponentName, NO_ORG_FOUND_FOR_GIVEN_ID);
            throw new ResourceNotFoundException(NO_ORG_FOUND_FOR_GIVEN_ID);
        }
    }

    public void validateOrganisationExistsAndActive(String orgId) {
        var org = Optional.ofNullable(organisationService
                .getOrganisationByOrgIdentifier(orgId));

        if (org.isEmpty()) {
            log.error(LOG_TWO_ARG_PLACEHOLDER, loggingComponentName, NO_ORG_FOUND_FOR_GIVEN_ID);
            throw new ResourceNotFoundException(NO_ORG_FOUND_FOR_GIVEN_ID);
        }
        validateOrganisationIsActive(org.get(), BAD_REQUEST);
    }

    public void validateGetRefreshUsersParams(String since, String userId, Integer pageSize, UUID searchAfter) {
        if ((since == null && userId == null) || (since != null && userId != null)) {
            throw new InvalidRequest(INVALID_MANDATORY_PARAMETER);
        }

        if (since != null) {
            if (!isSinceInValidFormat(since)) {
                throw new InvalidRequest(INVALID_SINCE_TIMESTAMP + SINCE_TIMESTAMP_FORMAT);
            }
            if (searchAfter != null && (pageSize == null || pageSize <= 0)) {
                throw new InvalidRequest(INVALID_PAGE_INFORMATION);
            }
        }
    }

    public void validateSince(String since) {
        if (since != null && !isSinceInValidFormat(since)) {
            throw new InvalidRequest(INVALID_SINCE_TIMESTAMP + LAST_UPDATED_SINCE_TIMESTAMP_FORMAT);
        }
    }

    private boolean isSinceInValidFormat(String since) {
        try {
            LocalDateTime.parse(since, ISO_DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return false;
        }

        return true;
    }

    public void validateDxAddress(UpdateContactInformationRequest updateContactInformationRequest) {

        String dxNumber = updateContactInformationRequest.getDxNumber();
        String dxExchange = updateContactInformationRequest.getDxExchange();

        if ((StringUtils.isNotEmpty(dxNumber) && StringUtils.isNotBlank(dxNumber))
            && (StringUtils.isEmpty(dxExchange) || StringUtils.isBlank(dxExchange))) {
            throw new FieldAndPersistenceValidationException(HttpStatus.valueOf(400),
                "Organisation dxExchange canot be empty be empty");
        }
        if ((StringUtils.isNotEmpty(dxExchange) && StringUtils.isNotBlank(dxExchange))
            && (StringUtils.isEmpty(dxNumber) || StringUtils.isBlank(dxNumber))) {
            throw new FieldAndPersistenceValidationException(HttpStatus.valueOf(400),
                "Organisation dxNumber canot be empty be empty");
        }
        if (((StringUtils.isNotEmpty(dxNumber) && StringUtils.isNotBlank(dxNumber))
            && (StringUtils.isNotEmpty(dxExchange) && StringUtils.isNotBlank(dxExchange)))
            && dxNumber.length() >= 14) {
            throw new FieldAndPersistenceValidationException(HttpStatus.valueOf(400),
                "DX Number (max=13) has invalid length");
        }
        if (((StringUtils.isNotEmpty(dxNumber) && StringUtils.isNotBlank(dxNumber))
            && (StringUtils.isNotEmpty(dxExchange) && StringUtils.isNotBlank(dxExchange)))
            && dxExchange.length() >= 40) {
            throw new FieldAndPersistenceValidationException(HttpStatus.valueOf(400),
                "DX Exchange (max=40) has invalid length");
        }
        if (((StringUtils.isNotEmpty(dxNumber) && StringUtils.isNotBlank(dxNumber))
            && (StringUtils.isNotEmpty(dxExchange) && StringUtils.isNotBlank(dxExchange)))
            && !dxNumber.matches("^[a-zA-Z0-9 ]*$")) {
            throw new FieldAndPersistenceValidationException(HttpStatus.valueOf(400),
                "Invalid Dx Number entered: " + dxNumber + ", it can only contain "
                    .concat("numbers, letters and spaces"));
        }

    }

    public void validateAddress(UpdateContactInformationRequest updateContactInformationRequest) {
        String uprn = updateContactInformationRequest.getUprn();
        String addressLine1 = updateContactInformationRequest.getAddressLine1();

        if (StringUtils.isEmpty(addressLine1) || StringUtils.isBlank(addressLine1)) {
            throw new FieldAndPersistenceValidationException(HttpStatus.valueOf(400),
                "AddressLine1 cannot be empty");
        }
        if (StringUtils.isNotEmpty(uprn.trim()) && uprn.length() > 14) {
            throw new FieldAndPersistenceValidationException(HttpStatus.valueOf(400),
                "Uprn must not be greater than 14 characters long");
        }
    }
}
