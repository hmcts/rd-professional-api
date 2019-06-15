package uk.gov.hmcts.reform.professionalapi.controller.advice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.DATA_INTEGRITY_VIOLATION;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.EMPTY_RESULT_DATA_ACCESS;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.INVALID_REQUEST;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.MALFORMED_JSON;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.METHOD_ARG_NOT_VALID;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.UNKNOWN_EXCEPTION;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.UNSUPPORTED_MEDIA_TYPES;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpStatusCodeException;

import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.professionalapi.controller")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class ExceptionMapper {

    private static final Logger LOG                         = LoggerFactory.getLogger(ExceptionMapper.class);
    private static final String HANDLING_EXCEPTION_TEMPLATE = "handling exception: {}";

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<Object> handleEmptyResultDataAccessException(
            EmptyResultDataAccessException ex) {
        return errorDetailsResponseEntity(ex, NOT_FOUND, EMPTY_RESULT_DATA_ACCESS);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)

    public ResponseEntity<Object> annotationDrivenValidationError(
            MethodArgumentNotValidException ex) {
        return errorDetailsResponseEntity(ex, BAD_REQUEST, METHOD_ARG_NOT_VALID);
    }

    @ExceptionHandler(InvalidRequest.class)
    public ResponseEntity<Object> customValidationError(
            InvalidRequest ex) {
        return errorDetailsResponseEntity(ex, BAD_REQUEST, INVALID_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)

    public ResponseEntity<Object> dataIntegrityViolationError(DataIntegrityViolationException ex) {

        return errorDetailsResponseEntity(ex, BAD_REQUEST, DATA_INTEGRITY_VIOLATION);

    }

    @ExceptionHandler(IllegalArgumentException.class)

    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {

        return errorDetailsResponseEntity(ex, BAD_REQUEST, INVALID_REQUEST);
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<Object> handleHttpStatusException(HttpStatusCodeException ex) {
        HttpStatus httpStatus = ex.getStatusCode();
        return errorDetailsResponseEntity(ex, httpStatus, httpStatus.getReasonPhrase());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)

    public ResponseEntity<Object> httpMessageNotReadableExceptionError(HttpMessageNotReadableException ex) {

        return errorDetailsResponseEntity(ex, BAD_REQUEST, MALFORMED_JSON);

    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            IllegalArgumentException ex) {

        return errorDetailsResponseEntity(ex, BAD_REQUEST, UNSUPPORTED_MEDIA_TYPES);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        return errorDetailsResponseEntity(ex, INTERNAL_SERVER_ERROR, UNKNOWN_EXCEPTION);
    }

    private String getTimeStamp() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.ENGLISH).format(new Date());
    }

    private static Throwable getRootException(Throwable exception) {
        Throwable rootException = exception;
        while (rootException.getCause() != null) {
            rootException = rootException.getCause();
        }
        return rootException;
    }

    private ResponseEntity<Object> errorDetailsResponseEntity(Exception ex, HttpStatus httpStatus, String errorMsg) {

        LOG.error(HANDLING_EXCEPTION_TEMPLATE, ex.getMessage());
        ErrorResponse errorDetails = ErrorResponse.builder()
                .errorDescription(getRootException(ex).getLocalizedMessage())
                .errorMessage(errorMsg)
                .status(httpStatus).errorCode(httpStatus.value())
                .timeStamp(getTimeStamp())
                .build();

        return new ResponseEntity<>(
                errorDetails, httpStatus);
    }
}
