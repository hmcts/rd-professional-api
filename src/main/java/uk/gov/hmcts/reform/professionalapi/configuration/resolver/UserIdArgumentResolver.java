package uk.gov.hmcts.reform.professionalapi.configuration.resolver;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;

import javax.servlet.http.HttpServletRequest;


@Slf4j
public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    IdamRepository idamRepository;

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
        //Inside UserIdArgumentResolver
        UserInfo userInfo = idamRepository.getUserInfo(getUserToken());
        if (null != userInfo && StringUtils.isNotEmpty(userInfo.getUid())) {
            userId = userInfo.getUid().trim();
        }
        return userId;
    }

    public String getUserToken() {
        var jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getTokenValue();
    }
}
