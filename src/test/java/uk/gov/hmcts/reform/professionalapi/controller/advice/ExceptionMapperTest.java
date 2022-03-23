package uk.gov.hmcts.reform.professionalapi.controller.advice;

import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.validation.ConstraintViolationException;

import com.fasterxml.jackson.databind.JsonMappingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.exception.ForbiddenException;

import java.util.Collections;
import java.util.LinkedList;

@ExtendWith(MockitoExtension.class)
class ExceptionMapperTest {

    @InjectMocks
    private ExceptionMapper exceptionMapper;

    @Mock
    LinkedList<JsonMappingException.Reference> path = new LinkedList<>();

    @Test
    void test_handle_empty_result_exception() {
        EmptyResultDataAccessException emptyResultDataAccessException = new EmptyResultDataAccessException(1);

        ResponseEntity<Object> responseEntity
                = exceptionMapper.handleEmptyResultDataAccessException(emptyResultDataAccessException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals(emptyResultDataAccessException.getMessage(),
                ((ErrorResponse) responseEntity.getBody()).getErrorDescription());
    }

    @Test
    void test_handle_resource_not_found_exception() {
        ResourceNotFoundException resourceNotFoundException = new ResourceNotFoundException("Resource not found");

        ResponseEntity<Object> responseEntity
                = exceptionMapper.handleResourceNotFoundException(resourceNotFoundException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Resource not found", ((ErrorResponse) responseEntity.getBody()).getErrorDescription());

    }

    @Test
    void test_handle_illegal_argument_exception() {
        IllegalArgumentException exception = new IllegalArgumentException();

        ResponseEntity<Object> responseEntity = exceptionMapper.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(exception.getMessage(), ((ErrorResponse) responseEntity.getBody()).getErrorDescription());

    }

    @Test
    void test_handle_http_message_not_readable_exception() {
        HttpMessageNotReadableException httpMessageNotReadableException = mock(HttpMessageNotReadableException.class);
        JsonMappingException jm = mock(JsonMappingException.class);
        JsonMappingException.Reference rf = mock(JsonMappingException.Reference.class);
        when(httpMessageNotReadableException.getCause()).thenReturn(jm);
        when(jm.getPath()).thenReturn(Collections.unmodifiableList(path));
        when(jm.getPath().get(0)).thenReturn(rf);
        when(jm.getPath().get(0).getFieldName()).thenReturn("field");
        ResponseEntity<Object> responseEntity = exceptionMapper
                .httpMessageNotReadableExceptionError(httpMessageNotReadableException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    void test_handle_http_media_type_not_supported_exception() {
        IllegalArgumentException exception = new IllegalArgumentException();

        ResponseEntity<Object> responseEntity = exceptionMapper.handleHttpMediaTypeNotSupported(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(exception.getMessage(), ((ErrorResponse) responseEntity.getBody()).getErrorDescription());

    }

    @Test
    void test_handle_forbidden_error_exception() {
        AccessDeniedException exception = new AccessDeniedException("Access Denied");

        ResponseEntity<Object> responseEntity = exceptionMapper.handleForbiddenException(exception);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertEquals(exception.getMessage(), ((ErrorResponse) responseEntity.getBody()).getErrorDescription());

    }

    @Test
    void test_handle_http_status_code_exception() {
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        HttpStatus httpStatus = mock(HttpStatus.class);

        when(exception.getStatusCode()).thenReturn(httpStatus);

        ResponseEntity<Object> responseEntity = exceptionMapper.handleHttpStatusException(exception);
        assertNotNull(responseEntity.getStatusCode());
        assertEquals(exception.getMessage(), ((ErrorResponse) responseEntity.getBody()).getErrorDescription());

    }

    @Test
    void test_handle_exception() {
        Exception exception = new Exception();

        ResponseEntity<Object> responseEntity = exceptionMapper.handleException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(exception.getMessage(), ((ErrorResponse) responseEntity.getBody()).getErrorDescription());

    }

    @Test
    void test_handle_method_not_valid_exception() {
        MethodArgumentNotValidException methodArgumentNotValidException = mock(MethodArgumentNotValidException.class);

        ResponseEntity<Object> responseEntity
                = exceptionMapper.annotationDrivenValidationError(methodArgumentNotValidException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(methodArgumentNotValidException.getMessage(), ((ErrorResponse) responseEntity.getBody())
                .getErrorDescription());

    }

    @Test
    void test_handle_invalid_request_exception() {
        InvalidRequest invalidRequestException = new InvalidRequest("Invalid Request");

        ResponseEntity<Object> responseEntity = exceptionMapper.customValidationError(invalidRequestException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(invalidRequestException.getMessage(), ((ErrorResponse) responseEntity.getBody())
                .getErrorDescription());

    }

    @Test
    void test_handle_external_api_exception() {
        ExternalApiException externalApiException = mock(ExternalApiException.class);
        when(externalApiException.getHttpStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

        ResponseEntity<Object> responseEntity = exceptionMapper.getUserProfileExceptionError(externalApiException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals(externalApiException.getMessage(), ((ErrorResponse) responseEntity.getBody())
                .getErrorDescription());

    }

    @Test
    void test_handle_duplicate_key_exception() {
        DuplicateKeyException duplicateKeyException = new DuplicateKeyException("Duplicate Key Exception");

        ResponseEntity<Object> responseEntity = exceptionMapper.duplicateKeyException(duplicateKeyException);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertEquals(duplicateKeyException.getMessage(), ((ErrorResponse) responseEntity.getBody())
                .getErrorDescription());

    }

    @Test
    void test_handle_constraint_violation_exception() {
        ConstraintViolationException constraintViolationException
                = new ConstraintViolationException("Constraint Violation", null);

        ResponseEntity<Object> responseEntity = exceptionMapper.constraintViolationError(constraintViolationException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(constraintViolationException.getMessage(), ((ErrorResponse) responseEntity.getBody())
                .getErrorDescription());

    }

    @Test
    void test_handle_data_integrity_violation_exception() {
        DataIntegrityViolationException dataIntegrityViolationException
                = new DataIntegrityViolationException("Data Integrity Violation");

        ResponseEntity<Object> responseEntity
                = exceptionMapper.dataIntegrityViolationError(dataIntegrityViolationException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(dataIntegrityViolationException.getMessage(),
                ((ErrorResponse) responseEntity.getBody()).getErrorDescription());

    }

    @Test
    void test_handle_data_integrity_violation_exception_with_custom_messages() {
        String errorCause = "SRA_ID";
        ResponseEntity<Object> responseEntity
                = generateCustomDataIntegrityViolationResponseMessageForSpecificCause(errorCause);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(((ErrorResponse) responseEntity.getBody()).getErrorMessage().contains(errorCause));

        errorCause = "COMPANY_NUMBER";
        ResponseEntity<Object> responseEntity1
                = generateCustomDataIntegrityViolationResponseMessageForSpecificCause(errorCause);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity1.getStatusCode());
        assertTrue(((ErrorResponse) responseEntity1.getBody()).getErrorMessage().contains(errorCause));

        errorCause = "EMAIL_ADDRESS";
        ResponseEntity<Object> responseEntity2
                = generateCustomDataIntegrityViolationResponseMessageForSpecificCause(errorCause);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity2.getStatusCode());
        assertTrue(((ErrorResponse) responseEntity2.getBody()).getErrorMessage().contains("EMAIL"));

        errorCause = "PBA_NUMBER";
        ResponseEntity<Object> responseEntity3
                = generateCustomDataIntegrityViolationResponseMessageForSpecificCause(errorCause);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity3.getStatusCode());
        assertTrue(((ErrorResponse) responseEntity3.getBody()).getErrorMessage().contains(errorCause));
    }

    @Test
    void test_handle_DuplicateUserException() {
        HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.CONFLICT);

        ResponseEntity<Object> responseEntity = exceptionMapper.handleDuplicateUserException(httpClientErrorException);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
        assertEquals(httpClientErrorException.getMessage(),
                ((ErrorResponse) responseEntity.getBody()).getErrorDescription());
    }

    @Test
    void test_handle_launchDarkly_exception() {
        ForbiddenException forbiddenException = new ForbiddenException("LD Forbidden Exception");
        ResponseEntity<Object> responseEntity = exceptionMapper.handleLaunchDarklyException(forbiddenException);
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertEquals(forbiddenException.getMessage(), ((ErrorResponse) responseEntity.getBody())
                .getErrorDescription());
    }

    private ResponseEntity<Object> generateCustomDataIntegrityViolationResponseMessageForSpecificCause(
            String errorCause) {
        DataIntegrityViolationException dataIntegrityViolationException = mock(DataIntegrityViolationException.class);

        Throwable throwable = mock(Throwable.class);
        Throwable throwable1 = mock(Throwable.class);

        when(dataIntegrityViolationException.getCause()).thenReturn(throwable);
        when(dataIntegrityViolationException.getCause().getCause()).thenReturn(throwable1);
        when(dataIntegrityViolationException.getCause().getCause().getMessage()).thenReturn(errorCause);

        return exceptionMapper.dataIntegrityViolationError(dataIntegrityViolationException);
    }
}
