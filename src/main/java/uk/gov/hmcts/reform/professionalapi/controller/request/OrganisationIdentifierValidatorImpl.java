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
import uk.gov.hmcts.reform.professionalapi.util.PbaAccountUtil;

@Component
@Slf4j
public class OrganisationIdentifierValidatorImpl implements UpdateOrganisationValidator {

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

        boolean isPuiFinanceManExist = false;
        for (GrantedAuthority authority : authorities) {

            if (!StringUtils.isEmpty(authority.getAuthority()) && "pui-finance-manager".equals(authority.getAuthority().trim())) {

                isPuiFinanceManExist = true;
                break;
            }

        }

        if (!isPuiFinanceManExist) {
            authorities.forEach(role
                -> {
                PbaAccountUtil.validateOrgIdentifier(extOrgIdentifier, organisation.getOrganisationIdentifier());
            });
        }
    }
}
