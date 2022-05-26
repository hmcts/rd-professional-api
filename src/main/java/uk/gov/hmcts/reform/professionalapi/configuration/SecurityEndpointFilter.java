package uk.gov.hmcts.reform.professionalapi.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.professionalapi.controller.advice.UnauthorizedException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Configuration
public class SecurityEndpointFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION = "Authorization";
    public static final String SERVICE_AUTHORIZATION2 = "ServiceAuthorization";

    @Autowired
    IdamApi idamApi;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            Throwable throwable = e.getCause();
            if (e instanceof UnauthorizedException) {
                logger.error("Authorisation exception", e);
                response.sendError(HttpStatus.FORBIDDEN.value(), "Access Denied");
                return;
            }
            throw e;
        }
    }
}
