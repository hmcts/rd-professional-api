package uk.gov.hmcts.reform.professionalapi.configuration.resolver;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MESSAGE_403_FORBIDDEN;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;


@Component
@Slf4j
public class OrganisationIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    ProfessionalUserRepository professionalUserRepository;

    @Autowired
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {

        return null != methodParameter.getParameterAnnotation(OrgId.class);
    }

    @Override
    public Object resolveArgument(
        MethodParameter methodParameter,
        ModelAndViewContainer modelAndViewContainer,
        NativeWebRequest nativeWebRequest,
        WebDataBinderFactory webDataBinderFactory) throws EmptyResultDataAccessException {

        HttpServletRequest request
            = (HttpServletRequest) nativeWebRequest.getNativeRequest();
        String userId;
        String orgId = null;
        ProfessionalUser professionalUser;
        Organisation organisation;

        UserInfo userInfo = jwtGrantedAuthoritiesConverter.getUserInfo();

        if (null != userInfo && StringUtils.isNotEmpty(userInfo.getUid())) {
            userId = userInfo.getUid();
            professionalUser = professionalUserRepository.findByUserIdentifier(userId.trim());
            if (null != professionalUser && null != professionalUser.getOrganisation()) {

                organisation = professionalUser.getOrganisation();
                orgId = organisation.getOrganisationIdentifier();
            } else {
                log.error("{}:: ProfessionalUserUser info null::", loggingComponentName);
                throw new AccessDeniedException(ERROR_MESSAGE_403_FORBIDDEN);
            }

        }

        if (null == userInfo || StringUtils.isEmpty(orgId)) {

            log.error("{}:: userInfo or OrganisationIdentifier is Null::", loggingComponentName);
            throw new AccessDeniedException(ERROR_MESSAGE_403_FORBIDDEN);
        }
        return orgId;
    }
}
