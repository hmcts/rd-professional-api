package uk.gov.hmcts.reform.professionalapi.controller.request.controller.advice;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.validation.ConstraintViolationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExceptionMapper;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ResourceNotFoundException;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionMapperTest {

    @InjectMocks
    private ExceptionMapper exceptionMapper;

    @Test
    public void should_handle_empty_result_exception() {
        EmptyResultDataAccessException emptyResultDataAccessException = mock(EmptyResultDataAccessException.class);

        ResponseEntity<Object> responseEntity = exceptionMapper.handleEmptyResultDataAccessException(emptyResultDataAccessException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void should_handle_resource_not_found_exception() {
        ResourceNotFoundException resourceNotFoundException = mock(ResourceNotFoundException.class);

        ResponseEntity<Object> responseEntity = exceptionMapper.handleResourceNotFoundException(resourceNotFoundException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void should_handle_illegal_argument_exception() {
        IllegalArgumentException exception = mock(IllegalArgumentException.class);

        ResponseEntity<Object> responseEntity = exceptionMapper.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void should_handle_http_message_not_readable_exception() {
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        ResponseEntity<Object> responseEntity = exceptionMapper.httpMessageNotReadableExceptionError(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void should_handle_http_media_type_not_supported_exception() {
        IllegalArgumentException exception = mock(IllegalArgumentException.class);

        ResponseEntity<Object> responseEntity = exceptionMapper.handleHttpMediaTypeNotSupported(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void should_handle_forbidden_error_exception() {
        AccessDeniedException exception = mock(AccessDeniedException.class);

        ResponseEntity<Object> responseEntity = exceptionMapper.handleForbiddenException(exception);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @Test
    public void should_handle_http_status_code_exception() {
        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        HttpStatus httpStatus = mock(HttpStatus.class);

        when(exception.getStatusCode()).thenReturn(httpStatus);

        ResponseEntity<Object> responseEntity = exceptionMapper.handleHttpStatusException(exception);
        assertNotNull(responseEntity.getStatusCode());
    }

    @Test
    public void should_handle_exception() {
        Exception exception = mock(Exception.class);

        ResponseEntity<Object> responseEntity = exceptionMapper.handleException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    public void should_handle_method_not_valid_exception() {
        MethodArgumentNotValidException methodArgumentNotValidException = mock(MethodArgumentNotValidException.class);

        ResponseEntity<Object> responseEntity = exceptionMapper.annotationDrivenValidationError(methodArgumentNotValidException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void should_handle_invalid_request_exception() {
        InvalidRequest invalidRequestException = mock(InvalidRequest.class);

        ResponseEntity<Object> responseEntity = exceptionMapper.customValidationError(invalidRequestException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void should_handle_external_api_exception() {
        ExternalApiException externalApiException = mock(ExternalApiException.class);
        when(externalApiException.getHttpStatus()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);

        ResponseEntity<Object> responseEntity = exceptionMapper.getUserProfileExceptionError(externalApiException);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }

    @Test
    public void should_handle_duplicate_key_exception() {
        DuplicateKeyException duplicateKeyException = mock(DuplicateKeyException.class);

        ResponseEntity<Object> responseEntity = exceptionMapper.duplicateKeyException(duplicateKeyException);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
    }

    @Test
    public void should_handle_constraint_violation_exception() {
        ConstraintViolationException constraintViolationException = mock(ConstraintViolationException.class);

        ResponseEntity<Object> responseEntity = exceptionMapper.constraintViolationError(constraintViolationException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void should_handle_data_integrity_violation_exception() {
        DataIntegrityViolationException dataIntegrityViolationException = mock(DataIntegrityViolationException.class);

        ResponseEntity<Object> responseEntity = exceptionMapper.dataIntegrityViolationError(dataIntegrityViolationException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void should_handle_data_integrity_violation_exception_with_custom_message_for_sra_id() {
        String errorCause = "SRA_ID";
        ResponseEntity<Object> responseEntity = generateCustomDataIntegrityViolationResponseMessageForSpecificCause(errorCause);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(((ErrorResponse)responseEntity.getBody()).getErrorMessage().contains(errorCause));
    }

    @Test
    public void should_handle_data_integrity_violation_exception_with_custom_message_for_company_number() {
        String errorCause = "COMPANY_NUMBER";
        ResponseEntity<Object> responseEntity = generateCustomDataIntegrityViolationResponseMessageForSpecificCause(errorCause);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(((ErrorResponse)responseEntity.getBody()).getErrorMessage().contains(errorCause));
    }

    @Test
    public void should_handle_data_integrity_violation_exception_with_custom_message_for_email_address() {
        String errorCause = "EMAIL_ADDRESS";
        ResponseEntity<Object> responseEntity = generateCustomDataIntegrityViolationResponseMessageForSpecificCause(errorCause);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(((ErrorResponse)responseEntity.getBody()).getErrorMessage().contains("EMAIL"));
    }

    @Test
    public void should_handle_Request_Rejected_Exception() {
        RequestRejectedException exception = mock(RequestRejectedException.class);
        ResponseEntity<Object> responseEntity = exceptionMapper.handleRequestRejectedException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void should_handle_data_integrity_violation_exception_with_custom_message_for_pba_number() {
        String errorCause = "PBA_NUMBER";
        ResponseEntity<Object> responseEntity = generateCustomDataIntegrityViolationResponseMessageForSpecificCause(errorCause);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertTrue(((ErrorResponse)responseEntity.getBody()).getErrorMessage().contains(errorCause));
    }

    private ResponseEntity<Object> generateCustomDataIntegrityViolationResponseMessageForSpecificCause(String errorCause) {
        DataIntegrityViolationException dataIntegrityViolationException = mock(DataIntegrityViolationException.class);

        Throwable throwable = mock(Throwable.class);
        Throwable throwable1 = mock(Throwable.class);

        when(dataIntegrityViolationException.getCause()).thenReturn(throwable);
        when(dataIntegrityViolationException.getCause().getCause()).thenReturn(throwable1);
        when(dataIntegrityViolationException.getCause().getCause().getMessage()).thenReturn(errorCause);

        return exceptionMapper.dataIntegrityViolationError(dataIntegrityViolationException);
    }

    @Test
    public void should_handle_DuplicateUserException() {
        HttpClientErrorException httpClientErrorException = mock(HttpClientErrorException.class);

        ResponseEntity<Object> responseEntity = exceptionMapper.handleDuplicateUserException(httpClientErrorException);

        assertEquals(HttpStatus.CONFLICT, responseEntity.getStatusCode());
    }
}
