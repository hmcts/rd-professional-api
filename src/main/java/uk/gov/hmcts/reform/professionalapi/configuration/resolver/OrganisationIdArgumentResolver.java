package uk.gov.hmcts.reform.professionalapi.configuration.resolver;

import java.util.UUID;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;


@Component
@Slf4j
public class OrganisationIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    ProfessionalUserRepository professionalUserRepository;

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
        log.info("professionalUserRepository Object::" + professionalUserRepository);
        ServiceAndUserDetails serviceAndUserDetails = (ServiceAndUserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        userId = serviceAndUserDetails.getUsername();
        if (null != serviceAndUserDetails && StringUtils.isNotEmpty(userId)) {

            professionalUser = professionalUserRepository.findByUserIdentifier(UUID.fromString(userId.trim()));
            if (null != professionalUser && null != professionalUser.getOrganisation()) {

                organisation = professionalUser.getOrganisation();
                orgId = organisation.getOrganisationIdentifier();
                log.info("OrganisationIdentifier::" + orgId);

            } else {
                log.error("ProfessionalUserUser info null::");
                throw new EmptyResultDataAccessException(1);
            }

        }

        return orgId;
    }
}
