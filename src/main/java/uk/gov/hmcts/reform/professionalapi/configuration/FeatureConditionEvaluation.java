package uk.gov.hmcts.reform.professionalapi.configuration;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MESSAGE_403_FORBIDDEN;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class FeatureConditionEvaluation implements HandlerInterceptor {

    @Value("${deleteOrganisationEnabled}")
    private boolean deleteOrganisationEnabled;

    @Value("${activeOrgsExternalEnabled}")
    private boolean activeOrgsExternalEnabled;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object arg2) throws Exception {

        if (!deleteOrganisationEnabled && request.getMethod().equals("DELETE")) {

            throw new AccessDeniedException(ERROR_MESSAGE_403_FORBIDDEN);
        }

        if (!activeOrgsExternalEnabled && request.getRequestURI()
                .contains("refdata/external/v1/organisations/status/")) {
            throw new AccessDeniedException(ERROR_MESSAGE_403_FORBIDDEN);
        }

        return true;
    }
}