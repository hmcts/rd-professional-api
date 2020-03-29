package uk.gov.hmcts.reform.professionalapi.authchecker.core.authorizer;

import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import uk.gov.hmcts.reform.professionalapi.authchecker.core.resolver.SubjectResolver;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.exception.AuthCheckerException;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.exception.BearerTokenInvalidException;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.exception.BearerTokenMissingException;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.exception.UnauthorisedServiceException;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.exception.ServiceTokenInvalidException;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.exception.ServiceTokenParsingException;
import uk.gov.hmcts.reform.professionalapi.authchecker.core.resolver.Service;


public class ServiceRequestAuthorizer implements RequestAuthorizer<Service> {

    public static final String AUTHORISATION = "ServiceAuthorization";

    private final SubjectResolver<Service> serviceResolver;
    private final Set<String> authorizedServices;

    public ServiceRequestAuthorizer(SubjectResolver<Service> serviceResolver, Set<String> authorizedServices) {
        this.serviceResolver = serviceResolver;
        this.authorizedServices = authorizedServices;
    }

    @Override
    public Service authorise(HttpServletRequest request) throws UnauthorisedServiceException {
        if (authorizedServices.isEmpty()) {
            throw new IllegalArgumentException("Must have at least one service defined");
        }

        String bearerToken = request.getHeader(AUTHORISATION);
        if (bearerToken == null) {
            throw new BearerTokenMissingException();
        }

        Service service = getTokenDetails(bearerToken);
        if (!authorizedServices.contains(service.getPrincipal().toLowerCase())) {
            throw new UnauthorisedServiceException();
        }

        return service;
    }

    private Service getTokenDetails(String bearerToken) {
        try {
            return serviceResolver.getTokenDetails(bearerToken);
        } catch (ServiceTokenInvalidException e) {
            throw new BearerTokenInvalidException(e);
        } catch (ServiceTokenParsingException e) {
            throw new AuthCheckerException("Error parsing JWT token", e);
        }
    }

}