package uk.gov.hmcts.reform.professionalapi.controller.advice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.ACCESS_EXCEPTION;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.CONFLICT_EXCEPTION;
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
import javax.validation.ConstraintViolationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
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
        return errorDetailsResponseEntity(ex, NOT_FOUND, EMPTY_RESULT_DATA_ACCESS.getErrorMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(
            ResourceNotFoundException ex) {
        return errorDetailsResponseEntity(ex, NOT_FOUND, EMPTY_RESULT_DATA_ACCESS.getErrorMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> annotationDrivenValidationError(
            MethodArgumentNotValidException ex) {
        return errorDetailsResponseEntity(ex, BAD_REQUEST, METHOD_ARG_NOT_VALID.getErrorMessage());
    }

    @ExceptionHandler(InvalidRequest.class)
    public ResponseEntity<Object> customValidationError(
            InvalidRequest ex) {
        return errorDetailsResponseEntity(ex, BAD_REQUEST, INVALID_REQUEST.getErrorMessage());
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<Object> getUserProfileExceptionError(
            ExternalApiException ex) {
        return errorDetailsResponseEntity(ex, ex.getHttpStatus(), ex.getErrorMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Object> duplicateKeyException(
            DuplicateKeyException ex) {
        return errorDetailsResponseEntity(ex, CONFLICT, CONFLICT_EXCEPTION.getErrorMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> dataIntegrityViolationError(DataIntegrityViolationException ex) {
        String errorMessage = DATA_INTEGRITY_VIOLATION.getErrorMessage();
        String fieldName = null;
        if (ex.getCause() != null && ex.getCause().getCause() != null && ex.getCause().getCause().getMessage() != null) {
            String message = ex.getCause().getCause().getMessage().toUpperCase();
            if (message.contains("SRA_ID")) {
                errorMessage = String.format(errorMessage, "SRA_ID");
            } else if (message.contains("COMPANY_NUMBER")) {
                errorMessage = String.format(errorMessage, "COMPANY_NUMBER");
            } else if (message.contains("EMAIL_ADDRESS")) {
                errorMessage = String.format(errorMessage, "EMAIL");
            } else if (message.contains("PBA_NUMBER")) {
                errorMessage = String.format(errorMessage, "PBA_NUMBER");
            }
        }
        return errorDetailsResponseEntity(ex, BAD_REQUEST, errorMessage);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> constraintViolationError(ConstraintViolationException ex) {
        return errorDetailsResponseEntity(ex, BAD_REQUEST, DATA_INTEGRITY_VIOLATION.getErrorMessage());

    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        return errorDetailsResponseEntity(ex, BAD_REQUEST, INVALID_REQUEST.getErrorMessage());
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<Object> handleHttpStatusException(HttpStatusCodeException ex) {
        HttpStatus httpStatus = ex.getStatusCode();
        return errorDetailsResponseEntity(ex, httpStatus, httpStatus.getReasonPhrase());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> httpMessageNotReadableExceptionError(HttpMessageNotReadableException ex) {
        return errorDetailsResponseEntity(ex, BAD_REQUEST, MALFORMED_JSON.getErrorMessage());

    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Object> handleHttpMediaTypeNotSupported(IllegalArgumentException ex) {
        return errorDetailsResponseEntity(ex, BAD_REQUEST, UNSUPPORTED_MEDIA_TYPES.getErrorMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleForbiddenException(Exception ex) {
        return errorDetailsResponseEntity(ex, FORBIDDEN, ACCESS_EXCEPTION.getErrorMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        return errorDetailsResponseEntity(ex, INTERNAL_SERVER_ERROR, UNKNOWN_EXCEPTION.getErrorMessage());
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

        LOG.error(HANDLING_EXCEPTION_TEMPLATE, ex.getMessage(), ex);
        ErrorResponse errorDetails = new ErrorResponse(errorMsg, getRootException(ex).getLocalizedMessage(), getTimeStamp());

        return new ResponseEntity<>(errorDetails, httpStatus);
    }
}
