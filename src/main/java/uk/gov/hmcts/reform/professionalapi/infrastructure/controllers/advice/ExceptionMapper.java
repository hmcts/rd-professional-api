package uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.advice;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.professionalapi.infrastructure.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class ExceptionMapper {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionMapper.class);
    private static final String HANDLING_EXCEPTION_TEMPLATE = "handling exception: {}";

    @ExceptionHandler(EmptyResultDataAccessException.class)
    protected ResponseEntity<String> handleEmptyResultDataAccessException(
        HttpServletRequest request,
        EmptyResultDataAccessException e
    ) {
        LOG.info(HANDLING_EXCEPTION_TEMPLATE, e.getMessage());
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void validationError(MethodArgumentNotValidException ex) {
        LOG.info(HANDLING_EXCEPTION_TEMPLATE, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void dataIntegrityViolationError(DataIntegrityViolationException ex) {
        LOG.info(HANDLING_EXCEPTION_TEMPLATE, ex.getMessage());
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
