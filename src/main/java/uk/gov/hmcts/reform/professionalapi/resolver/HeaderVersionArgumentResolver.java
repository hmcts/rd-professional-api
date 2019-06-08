package uk.gov.hmcts.reform.professionalapi.resolver;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;


@Slf4j
public class HeaderVersionArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {

        return methodParameter.getParameterAnnotation(OrgId.class) != null;
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
        log.info("Inside HeaderVersionArgumentResolver");
        ServiceAndUserDetails serviceAndUserDetails = (ServiceAndUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (null != serviceAndUserDetails) {
            userId = serviceAndUserDetails.getUsername();
            log.info("Inside HeaderVersionArgumentResolver::orgId::" + userId);
            Object[] roles  =  serviceAndUserDetails.getAuthorities().toArray();
            String serviceName = serviceAndUserDetails.getServicename();
        }

        return userId;
    }
}
