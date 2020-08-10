package uk.gov.hmcts.reform.professionalapi.configuration;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MESSAGE_403_FORBIDDEN;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Component
public class FeatureConditionEvaluation  implements HandlerInterceptor {


    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object arg2) throws Exception {

        if (request.getMethod().equals("DELETE")) {

            response.sendError(403, ERROR_MESSAGE_403_FORBIDDEN);
        }
        return true;
    }


}
