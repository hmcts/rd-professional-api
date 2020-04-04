package uk.gov.hmcts.reform.professionalapi.configuration;

import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.resolver.Service;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.service.ServiceAndUserDetails;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.service.ServiceAndUserPair;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.service.ServiceDetails;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.service.User;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.service.UserDetails;

public class AuthCheckerUserDetailsService  implements AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {
    @Override
    public org.springframework.security.core.userdetails.UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
        Object principal = token.getPrincipal();

        if (principal instanceof Service) {
            return new ServiceDetails(((Service) principal).getPrincipal());
        }

        if (principal instanceof User) {
            User user = (User) principal;
            return new UserDetails(user.getPrincipal(), (String) token.getCredentials(), user.getRoles());
        }

        ServiceAndUserPair serviceAndUserPair = (ServiceAndUserPair) principal;
        return new ServiceAndUserDetails(
                serviceAndUserPair.getUser().getPrincipal(),
                (String) token.getCredentials(),
                serviceAndUserPair.getUser().getRoles(),
                serviceAndUserPair.getService().getPrincipal()
        );
    }
}

