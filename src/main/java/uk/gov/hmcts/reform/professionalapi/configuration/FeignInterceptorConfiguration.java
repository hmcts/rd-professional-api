package uk.gov.hmcts.reform.professionalapi.configuration;

import feign.RequestInterceptor;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import static uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter.AUTHORISATION;


@Slf4j
public class FeignInterceptorConfiguration {

    @Autowired
    AuthTokenGenerator authTokenGenerator;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Bean
    public RequestInterceptor requestInterceptor(FeignHeaderConfig config) {
        return requestTemplate -> {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String name = headerNames.nextElement();
                        String value = request.getHeader(name);
                        if (config.getHeaders().contains(name.toLowerCase())) {
                            if (name.equalsIgnoreCase(AUTHORISATION)) {
                                value = authTokenGenerator.generate();
                                log.info("S2S Value::::: " + value);
                            }
                            requestTemplate.header(name, value);
                        }
                    }
                } else {
                    log.warn("{}:: FeignHeadConfiguration {}", loggingComponentName, "Failed to get request header!");
                }
            }
        };
    }
}
