package uk.gov.hmcts.reform.professionalapi.util;

import com.auth0.jwt.JWT;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.reform.professionalapi.exception.ForbiddenException;
import uk.gov.hmcts.reform.professionalapi.service.impl.FeatureToggleServiceImpl;

import static net.logstash.logback.encoder.org.apache.commons.lang3.BooleanUtils.negate;

@Component
@AllArgsConstructor
public class FeatureConditionEvaluation implements HandlerInterceptor {

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String BEARER = "Bearer ";

    @Autowired
    private final FeatureToggleServiceImpl featureToggleService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {

        Map<String, String> launchDarklyUrlMap = featureToggleService.getLaunchDarklyMap();

        String restMethod = ((HandlerMethod) handler).getMethod().getName();
        String clazz = ((HandlerMethod) handler).getMethod().getDeclaringClass().getSimpleName();
        boolean flagStatus = Boolean.TRUE;

        if (negate(launchDarklyUrlMap.isEmpty()) && launchDarklyUrlMap.get(clazz + "." + restMethod) != null) {
            flagStatus = featureToggleService
                .isFlagEnabled(getServiceName(), launchDarklyUrlMap.get(clazz + "." + restMethod));

            if (!flagStatus) {
                throw new ForbiddenException("Forbidden with Launch Darkly");
            }
        }
        return flagStatus;
    }

    public String getServiceName() {
        HttpServletRequest request =
            ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();

        return JWT.decode(removeBearerFromToken(request.getHeader(SERVICE_AUTHORIZATION))).getSubject();
    }

    private String removeBearerFromToken(String token) {
        if (negate(token.startsWith(BEARER))) {
            return token;
        } else {
            return token.substring(BEARER.length());
        }
    }

}
