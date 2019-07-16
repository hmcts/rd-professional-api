package uk.gov.hmcts.reform.professionalapi.configuration;

import feign.RequestInterceptor;

import java.util.Enumeration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Slf4j
public class FeignInterceptorConfiguration {


    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                // If you take the following method in the cookie
                Cookie[] cookies = request.getCookies();
                if (cookies != null && cookies.length > 0) {
                    for (Cookie cookie : cookies) {
                        requestTemplate.header(cookie.getName(), cookie.getValue());
                    }
                } else {
                    log.warn("FeignHeadConfiguration", "Failed to get a cookie!");
                }

                Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String name = headerNames.nextElement();
                        String value = request.getHeader(name);
                        requestTemplate.header(name, value);
                    }
                } else {
                    log.warn("FeignHeadConfiguration", "Failed to get request header!");
                }
            }
        };
    }
}
