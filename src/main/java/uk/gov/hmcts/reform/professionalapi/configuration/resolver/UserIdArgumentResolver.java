package uk.gov.hmcts.reform.professionalapi.configuration.resolver;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;


@Slf4j
public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {

        return null != methodParameter.getParameterAnnotation(UserId.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter methodParameter,
            ModelAndViewContainer modelAndViewContainer,
            NativeWebRequest nativeWebRequest,
            WebDataBinderFactory webDataBinderFactory) throws Exception {

        HttpServletRequest request
                = (HttpServletRequest) nativeWebRequest.getNativeRequest();
        String userId = null;
        log.info("Inside UserIdArgumentResolver");
        ServiceAndUserDetails serviceAndUserDetails = (ServiceAndUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (null != serviceAndUserDetails && StringUtils.isNotEmpty(serviceAndUserDetails.getUsername())) {
            userId = serviceAndUserDetails.getUsername().trim();
            log.info("Inside UserIdArgumentResolver::userId::" + userId);
            Object[] roles  =  serviceAndUserDetails.getAuthorities().toArray();
            String serviceName = serviceAndUserDetails.getServicename();
        }

        return userId;
    }
}
