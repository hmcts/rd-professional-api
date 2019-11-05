package uk.gov.hmcts.reform.professionalapi.controller.request;

import java.util.Collection;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;

@Component
@Slf4j
public class OrganisationIdentifierIdentifierValidatorImpl implements OrganisationIdentifierValidator {

    @Override
    public void validate(Organisation existingOrganisation, OrganisationStatus inputStatus, String inputOrganisationIdentifier) {
        log.info("Into Organisation identifier validator...");
        checkOrganisationDoesNotExist(existingOrganisation, inputOrganisationIdentifier);
        log.info("Validation completed for identifier Organisation...");
    }

    private void checkOrganisationDoesNotExist(Organisation organisation, String inputOrganisationIdentifier) {
        if (null == organisation) {
            String errorMessage = "Organisation not found with organisationIdentifier: " + inputOrganisationIdentifier;
            log.error(errorMessage);
            throw new EmptyResultDataAccessException(errorMessage, 1);
        }
    }

    public void verifyExtUserOrgIdentifier(Organisation organisation, String extOrgIdentifier) {

        if (null == organisation || organisation.getPaymentAccounts().isEmpty()) {
            throw new EmptyResultDataAccessException(1);

        } else if (!extOrgIdentifier.trim().equals(organisation.getOrganisationIdentifier().trim())) {
            throw new AccessDeniedException("403 Forbidden");
        }
    }

    public void verifyNonPuiFinanceManagerOrgIdentifier(Collection<GrantedAuthority> authorities, Organisation organisation, String extOrgIdentifier) {

        boolean isPuiFinanceManExist = ifUserRoleExists(authorities, "pui-finance-manager");

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
            log.error("Organisation is not Active hence not returning any users");
            throw new EmptyResultDataAccessException(1);
        }
    }
}
