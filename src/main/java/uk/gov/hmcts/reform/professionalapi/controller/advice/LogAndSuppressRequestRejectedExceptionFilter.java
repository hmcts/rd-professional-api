package uk.gov.hmcts.reform.professionalapi.controller.advice;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.sql.Timestamp;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.INVALID_REQUEST;


@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LogAndSuppressRequestRejectedExceptionFilter extends GenericFilterBean {

    private final Gson gson = new Gson();

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
            ServletException {
        try {
            chain.doFilter(req, res);
        } catch (RequestRejectedException exception) {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;

            log.warn("{}:: request_rejected: remote={}, user_agent={}, request_url={}",
                    loggingComponentName, request.getRemoteHost(),
                    request.getHeader(HttpHeaders.USER_AGENT), request.getRequestURL(), exception);


            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(400);
            response.getWriter().print(this.gson.toJson(new ErrorResponse(INVALID_REQUEST.getErrorMessage(),
                    "The request was rejected because the URL is potentially malicious",
                    new Timestamp(System.currentTimeMillis()).toString())));
        }
    }
}