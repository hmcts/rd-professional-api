package uk.gov.hmcts.reform.professionalapi.controller;

import static org.slf4j.LoggerFactory.getLogger;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.slf4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;


@RequestMapping(
        path = "/test",
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE
)

@RestController
public class HelloController {

    private static final Logger LOG = getLogger(HelloController.class);
    private static final String MESSAGE = "Hello";


    @ApiOperation("Hello message")
    @ApiResponses({
            @ApiResponse(
                    code = 200,
                    message = "Hello message",
                    response = String.class
            )
    })
    @GetMapping(
            path = "/hello",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @ResponseBody
    public ResponseEntity<String> hello() {

        LOG.info("hello message ");
        ServiceAndUserDetails serviceAndUserDetails = (ServiceAndUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity
                .status(201)
                .body("{\"message\": \"" + MESSAGE + ":" + serviceAndUserDetails.getUsername() + " : " + serviceAndUserDetails.getAuthorities() + " : " + serviceAndUserDetails.getServicename() + "\"}");
    }
}
