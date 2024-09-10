package uk.gov.hmcts.reform.professionalapi.controller.advice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidContactInformations;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.ContactInformationValidationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.MFAStatus;
import uk.gov.hmcts.reform.professionalapi.exception.ForbiddenException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.ACCESS_EXCEPTION;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.CONFLICT_EXCEPTION;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.DATA_INTEGRITY_VIOLATION;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.DUPLICATE_USER;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.EMPTY_RESULT_DATA_ACCESS;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.INVALID_REQUEST;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.MALFORMED_JSON;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.METHOD_ARG_NOT_VALID;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.UNKNOWN_EXCEPTION;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ErrorConstants.UNSUPPORTED_MEDIA_TYPES;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.INVALID_MFA_VALUE;

@Slf4j
@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.professionalapi.controller")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class ExceptionMapper {

    private static final String HANDLING_EXCEPTION_TEMPLATE = "{}:: handling exception: {}";
    private static final Set<String> NEW_ENDPOINT_SUFFIXES = new HashSet<>();
    private static final String INTERNAL_V1_REQUEST_MAPPING = "refdata/internal/v1"
            + "/organisations";

    static {
        // Add all new endpoint suffixes to the set
        NEW_ENDPOINT_SUFFIXES.add("/name");
    }

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    private static String getTimeStamp() {
        return (LocalDateTime.now()).format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS"));
    }

    private static ResponseEntity<Object> collectErrors(MethodArgumentNotValidException ex) {
        // Get all the field errors
        final String errorMessages =
                ex.getBindingResult().getFieldErrors().stream()
                        .map(FieldError::getDefaultMessage) // Extract default message
                        .collect(Collectors.joining(", "));
        final ErrorResponse errorDetails =
                new ErrorResponse(METHOD_ARG_NOT_VALID.getErrorMessage(),
                        errorMessages,
                        getTimeStamp());
        return new ResponseEntity<>(errorDetails, BAD_REQUEST);
    }

    private static ResponseEntity<Object> collectErrors(ConstraintViolationException ex) {

        // Get all the field errors
        final String errorMessages = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));

        final ErrorResponse errorDetails =
                new ErrorResponse(INVALID_REQUEST.getErrorMessage(),
                        errorMessages,
                        getTimeStamp());
        return new ResponseEntity<>(errorDetails, BAD_REQUEST);

    }

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

    private boolean isNewEndpointPoint() {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.nonNull(requestAttributes)) {
            HttpServletRequest request = requestAttributes.getRequest();
            String requestUri = request.getRequestURI();

            return requestUri.contains(INTERNAL_V1_REQUEST_MAPPING) && matchesNewEndpoint(requestUri);
        }
        return false;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> annotationDrivenValidationError(
            MethodArgumentNotValidException ex) {

        if (isNewEndpointPoint()) {
            return collectErrors(ex);
        }

        return errorDetailsResponseEntity(ex, BAD_REQUEST, METHOD_ARG_NOT_VALID.getErrorMessage());
    }

    private boolean matchesNewEndpoint(String requestUri) {
        // Check if the URI ends with any of the new endpoint suffixes
        return NEW_ENDPOINT_SUFFIXES.stream().anyMatch(requestUri::endsWith);
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

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParams(MissingServletRequestParameterException ex) {
        return errorDetailsResponseEntity(ex, BAD_REQUEST, INVALID_REQUEST.getErrorMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Object> duplicateKeyException(
            DuplicateKeyException ex) {
        return errorDetailsResponseEntity(ex, CONFLICT, CONFLICT_EXCEPTION.getErrorMessage());
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<Object> handleDuplicateUserException(
            HttpClientErrorException ex) {
        return errorDetailsResponseEntity(ex, CONFLICT, DUPLICATE_USER.getErrorMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> dataIntegrityViolationError(DataIntegrityViolationException ex) {
        String errorMessage = DATA_INTEGRITY_VIOLATION.getErrorMessage();
        if (ex.getCause() != null && ex.getCause().getCause() != null && ex.getCause().getCause()
                .getMessage() != null) {
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

        if (isNewEndpointPoint()) {
            return collectErrors(ex);
        }

        return errorDetailsResponseEntity(ex, BAD_REQUEST, DATA_INTEGRITY_VIOLATION.getErrorMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        return errorDetailsResponseEntity(ex, BAD_REQUEST, INVALID_REQUEST.getErrorMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
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

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Object> handleLaunchDarklyException(Exception ex) {
        return errorDetailsResponseEntity(ex, FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        return errorDetailsResponseEntity(ex, INTERNAL_SERVER_ERROR, UNKNOWN_EXCEPTION.getErrorMessage());
    }

    @ExceptionHandler(InvalidContactInformations.class)
    public ResponseEntity<Object> handleContactInformationException(InvalidContactInformations ex) {
        return errorDetailsContactInfoResponseEntity(BAD_REQUEST, ex.getContactInfoValidations());
    }

    private Throwable getRootException(Throwable exception) {
        Throwable rootException = exception;
        while (rootException.getCause() != null) {
            rootException = rootException.getCause();
        }
        return rootException;
    }

    private ResponseEntity<Object> errorDetailsResponseEntity(Exception ex, HttpStatus httpStatus, String errorMsg) {

        log.info(HANDLING_EXCEPTION_TEMPLATE, loggingComponentName, ex.getMessage(), ex);
        String errorDescription = getRootException(ex).getLocalizedMessage();

        if (mfaEnumException(ex)) {
            errorDescription = INVALID_MFA_VALUE;
        }

        ErrorResponse errorDetails = new ErrorResponse(errorMsg, errorDescription,
                getTimeStamp());

        return new ResponseEntity<>(errorDetails, httpStatus);
    }

    private ResponseEntity<Object> errorDetailsContactInfoResponseEntity(
            HttpStatus httpStatus,
            List<ContactInformationValidationResponse> contactInfoValidations) {

        return new ResponseEntity<>(contactInfoValidations, httpStatus);
    }

    private boolean mfaEnumException(Exception ex) {
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ifx = (InvalidFormatException) ex.getCause();
            return ifx.getTargetType().isEnum() && (ifx.getTargetType().equals(MFAStatus.class));
        }
        return false;
    }
}
