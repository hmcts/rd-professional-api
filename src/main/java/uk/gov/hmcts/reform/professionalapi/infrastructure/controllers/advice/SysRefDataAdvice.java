package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.advice;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

import uk.gov.hmcts.reform.professionalapi.domain.RequiredFieldMissingException;

@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.professionalapi.infrastructure.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class SysRefDataAdvice {

    private static final Logger LOG = LoggerFactory.getLogger(SysRefDataAdvice.class);

    @ExceptionHandler(RequiredFieldMissingException.class)
    protected ResponseEntity<String> handleRequiredFieldMissingException(
        HttpServletRequest request,
        RequiredFieldMissingException e
    ) {
        LOG.info("handling exception: {}", e.getMessage());
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    protected ResponseEntity<String> handleEmptyResultDataAccessException(
        HttpServletRequest request,
        EmptyResultDataAccessException e
    ) {
        LOG.info("handling exception: {}", e.getMessage());
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<String> handleException(
        HttpServletRequest request,
        Exception e
    ) {
        LOG.info("Exception: {}", e);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
