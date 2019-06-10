package uk.gov.hmcts.reform.professionalapi.controller.advice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.DATA_INTEGRITY_VIOLATION;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.EMPTY_RESULT_DATA_ACCESS;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.ILLEGAL_ARGUMENT;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.INVALID_REQUEST;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.MALFORMED_JSON;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.METHOD_ARG_NOT_VALID;
import static uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorConstants.UNSUPPORTED_MEDIA_TYPES;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.http.HTTPException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpStatusCodeException;

import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.professionalapi.controller")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class ExceptionMapper {

    private static final Logger LOG                         = LoggerFactory.getLogger(ExceptionMapper.class);
    private static final String HANDLING_EXCEPTION_TEMPLATE = "handling exception: {}";

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<Object> handleEmptyResultDataAccessException(HttpServletRequest request,
                                                                       EmptyResultDataAccessException e, HttpHeaders headers, HttpStatus status) {
        LOG.error(HANDLING_EXCEPTION_TEMPLATE, e.getMessage());
        ErrorResponse errorDetails = ErrorResponse.builder()
                .errorDescription(getRootException(e).getLocalizedMessage())
                .errorMessage(EMPTY_RESULT_DATA_ACCESS)
                .status(BAD_REQUEST).errorCode(BAD_REQUEST.value())
                .timeStamp(getTimeStamp())
                .build();

        return new ResponseEntity<>(
                errorDetails, new HttpHeaders(), BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> annotationDrivenValidationError(HttpServletRequest request,
                                                                  MethodArgumentNotValidException e, HttpHeaders headers, HttpStatus status) {
        LOG.error(HANDLING_EXCEPTION_TEMPLATE, e.getMessage());
        ErrorResponse errorDetails = ErrorResponse.builder()
                .errorDescription(getRootException(e).getLocalizedMessage())
                .errorMessage(METHOD_ARG_NOT_VALID)
                .status(BAD_REQUEST).errorCode(BAD_REQUEST.value())
                .timeStamp(getTimeStamp())
                .build();

        return new ResponseEntity<>(
                errorDetails, new HttpHeaders(), BAD_REQUEST);
    }


    @ExceptionHandler(InvalidRequest.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> customValidationError(HttpServletRequest request,
                                                        InvalidRequest ex, HttpHeaders headers, HttpStatus status) {
        LOG.error(HANDLING_EXCEPTION_TEMPLATE, ex.getMessage());
        ErrorResponse errorDetails = ErrorResponse.builder()
                .errorDescription(getRootException(ex).getLocalizedMessage())
                .errorMessage(INVALID_REQUEST)
                .status(BAD_REQUEST).errorCode(BAD_REQUEST.value())
                .timeStamp(getTimeStamp())
                .build();

        return new ResponseEntity<>(
                errorDetails, new HttpHeaders(), BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> dataIntegrityViolationError(HttpServletRequest request,
                                                              DataIntegrityViolationException ex, HttpHeaders headers, HttpStatus status) {

        LOG.error(HANDLING_EXCEPTION_TEMPLATE, ex.getMessage());
        ErrorResponse errorDetails = ErrorResponse.builder()
                .errorDescription(getRootException(ex).getLocalizedMessage())
                .errorMessage(DATA_INTEGRITY_VIOLATION)
                .status(BAD_REQUEST).errorCode(BAD_REQUEST.value())
                .timeStamp(getTimeStamp())
                .build();

        return new ResponseEntity<>(
                errorDetails, new HttpHeaders(), BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleIllegalArgumentException(HttpServletRequest request,
                                                                 IllegalArgumentException ex, HttpHeaders headers, HttpStatus status) {

        LOG.error(HANDLING_EXCEPTION_TEMPLATE, ex.getMessage());
        ErrorResponse errorDetails = ErrorResponse.builder()
                .errorDescription(getRootException(ex).getLocalizedMessage())
                .errorMessage(ILLEGAL_ARGUMENT)
                .status(BAD_REQUEST).errorCode(BAD_REQUEST.value())
                .timeStamp(getTimeStamp())
                .build();

        return new ResponseEntity<>(
                errorDetails, new HttpHeaders(), BAD_REQUEST);
    }

    @ExceptionHandler(HTTPException.class)
    public ResponseEntity<Object> handleHttpException(HttpServletRequest request, HTTPException ex) {
        LOG.info(HANDLING_EXCEPTION_TEMPLATE, ex.getMessage());
        return new ResponseEntity<>(HttpStatus.resolve(ex.getStatusCode()));
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<Object> handleHttpStatusException(HttpServletRequest request, HttpStatusCodeException ex) {
        LOG.info(HANDLING_EXCEPTION_TEMPLATE, ex.getMessage());
        ErrorResponse errorDetails = ErrorResponse.builder()
                .errorDescription(getRootException(ex).getLocalizedMessage())
                .errorMessage(EMPTY_RESULT_DATA_ACCESS)
                .status(BAD_REQUEST).errorCode(BAD_REQUEST.value())
                .timeStamp(getTimeStamp())
                .build();

        return new ResponseEntity<>(
                errorDetails, new HttpHeaders(), BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> httpMessageNotReadableExceptionError(HttpServletRequest request,
                                                                       HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status) {

        LOG.error(HANDLING_EXCEPTION_TEMPLATE, ex.getMessage());
        ErrorResponse errorDetails = ErrorResponse.builder()
                .errorDescription(getRootException(ex).getLocalizedMessage())
                .errorMessage(MALFORMED_JSON)
                .status(BAD_REQUEST).errorCode(BAD_REQUEST.value())
                .timeStamp(getTimeStamp())
                .build();

        return new ResponseEntity<>(
                errorDetails, new HttpHeaders(), BAD_REQUEST);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpServletRequest request,
                                                                 IllegalArgumentException ex, HttpHeaders headers, HttpStatus status) {

        LOG.error(HANDLING_EXCEPTION_TEMPLATE, ex.getMessage());
        ErrorResponse errorDetails = ErrorResponse.builder()
                .errorDescription(getRootException(ex).getLocalizedMessage())
                .errorMessage(UNSUPPORTED_MEDIA_TYPES)
                .status(BAD_REQUEST).errorCode(BAD_REQUEST.value())
                .timeStamp(getTimeStamp())
                .build();

        return new ResponseEntity<>(
                errorDetails, new HttpHeaders(), BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(
            HttpServletRequest request,
            Exception e) {
        LOG.info("Exception: {}", e);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
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
}
