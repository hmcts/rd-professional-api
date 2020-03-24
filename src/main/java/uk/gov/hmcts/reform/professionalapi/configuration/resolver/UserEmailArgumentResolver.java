package uk.gov.hmcts.reform.professionalapi.configuration.resolver;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiGeneratorConstants.ERROR_MESSAGE_403_FORBIDDEN;

@Slf4j
public class UserEmailArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {

        return null != methodParameter.getParameterAnnotation(UserEmail.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter methodParameter,
            ModelAndViewContainer modelAndViewContainer,
            NativeWebRequest nativeWebRequest,
            WebDataBinderFactory webDataBinderFactory) throws Exception {

        HttpServletRequest request
                = (HttpServletRequest) nativeWebRequest.getNativeRequest();
        String userEmail = null;
        //Inside UserEmailArgumentResolver
        ServiceAndUserDetails serviceAndUserDetails = (ServiceAndUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (null != serviceAndUserDetails && StringUtils.isNotEmpty(serviceAndUserDetails.getUsername())) {
            userEmail = serviceAndUserDetails.getUsername().trim();
        }
        if (null == serviceAndUserDetails || null == serviceAndUserDetails.getAuthorities()
                || StringUtils.isEmpty(userEmail)) {

            log.error(" ServiceAndUserDetails or User Email is Null::");
            throw new AccessDeniedException(ERROR_MESSAGE_403_FORBIDDEN);
        }
        return userEmail;
    }
}
