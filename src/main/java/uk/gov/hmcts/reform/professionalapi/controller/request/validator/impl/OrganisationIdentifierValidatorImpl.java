package uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.ERROR_MESSAGE_403_FORBIDDEN;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.NO_ORG_FOUND_FOR_GIVEN_ID;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.ORG_NOT_ACTIVE_NO_USERS_RETURNED;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.PUI_FINANCE_MANAGER;

import java.util.Collection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationIdentifierValidator;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

@Component
@Slf4j
public class OrganisationIdentifierValidatorImpl implements OrganisationIdentifierValidator {

    private OrganisationService organisationService;

    @Autowired
    public OrganisationIdentifierValidatorImpl(OrganisationService organisationService) {
        this.organisationService = organisationService;
    }

    @Override
    public void validate(Organisation existingOrganisation, OrganisationStatus inputStatus, String inputOrganisationIdentifier) {
        //Into Organisation identifier validator
        checkOrganisationDoesNotExist(existingOrganisation, inputOrganisationIdentifier);
        //Validation completed for identifier Organisation
    }

    private void checkOrganisationDoesNotExist(Organisation organisation, String inputOrganisationIdentifier) {
        if (null == organisation) {
            String errorMessage = NO_ORG_FOUND_FOR_GIVEN_ID + inputOrganisationIdentifier;
            log.error(errorMessage);
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

    public void verifyNonPuiFinanceManagerOrgIdentifier(Collection<GrantedAuthority> authorities, Organisation organisation, String extOrgIdentifier) {

        boolean isPuiFinanceManExist = ifUserRoleExists(authorities, PUI_FINANCE_MANAGER);

        if (!isPuiFinanceManExist) {
            authorities.forEach(role -> RefDataUtil.validateOrgIdentifier(extOrgIdentifier, organisation.getOrganisationIdentifier()));
        }
    }

    public boolean ifUserRoleExists(Collection<GrantedAuthority> authorities, String role) {
        boolean doesRoleExist = false;
        for (GrantedAuthority authority : authorities) {

            if (!StringUtils.isEmpty(authority.getAuthority()) && role.equals(authority.getAuthority().trim())) {
                doesRoleExist = true;
                break;
            }
        }
        return doesRoleExist;
    }

    public void validateOrganisationIsActive(Organisation existingOrganisation) {
        if (OrganisationStatus.ACTIVE != existingOrganisation.getStatus()) {
            log.error(ORG_NOT_ACTIVE_NO_USERS_RETURNED);
            throw new EmptyResultDataAccessException(1);
        }
    }

    public void validateOrganisationExistsWithGivenOrgId(String orgId) {
        if (null == organisationService.getOrganisationByOrgIdentifier(orgId)) {
            log.error(NO_ORG_FOUND_FOR_GIVEN_ID);
            throw new ResourceNotFoundException(NO_ORG_FOUND_FOR_GIVEN_ID);
        }
    }
}
