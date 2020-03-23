package uk.gov.hmcts.reform.professionalapi.authchecker.serviceonly;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.exception.AuthCheckerException;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.service.Service;

@Slf4j
public class AuthCheckerServiceOnlyFilter extends AbstractPreAuthenticatedProcessingFilter {


    protected final RequestAuthorizer<Service> serviceRequestAuthorizer;

    public AuthCheckerServiceOnlyFilter(RequestAuthorizer<Service> serviceRequestAuthorizer) {
        this.serviceRequestAuthorizer = serviceRequestAuthorizer;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        return authorizeService(request);
    }

    private Service authorizeService(HttpServletRequest request) {
        try {
            return serviceRequestAuthorizer.authorise(request);
        } catch (AuthCheckerException e) {
            log.warn("Unsuccessful service authentication", e);
            return null;
        }
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }
}
