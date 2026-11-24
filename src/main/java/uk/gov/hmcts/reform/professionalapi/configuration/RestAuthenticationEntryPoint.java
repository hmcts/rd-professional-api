package uk.gov.hmcts.reform.professionalapi.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;

import java.io.IOException;
import java.time.LocalDateTime;

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