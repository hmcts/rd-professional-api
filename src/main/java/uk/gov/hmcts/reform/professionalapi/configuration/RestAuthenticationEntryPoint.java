package uk.gov.hmcts.reform.professionalapi.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component("restAuthenticationEntryPoint")
@Slf4j
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authenticationException) throws IOException {

        var mapper = new ObjectMapper();
        var errorResponse = new ErrorResponse(
            authenticationException.getMessage(),
            "Authentication Exception",
            LocalDateTime.now().toString()
        );
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        var errorMessage = mapper.writeValueAsString(errorResponse);
        response.setHeader("UnAuthorized-Token-Error", errorMessage);
        log.error(errorMessage);

    }
}