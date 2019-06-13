package uk.gov.hmcts.reform.professionalapi.controller.request.controller.advice;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExceptionMapper;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionMapperTest {

    @InjectMocks
    private ExceptionMapper exceptionMapper;

    @Test
    public void should_handle_empty_result_exception() {

        EmptyResultDataAccessException emptyResultDataAccessException = mock(EmptyResultDataAccessException.class);

        ResponseEntity<Object> responseEntity =
                exceptionMapper.handleEmptyResultDataAccessException(emptyResultDataAccessException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

    }

    @Test
    public void should_handle_illegal_argument_exception() {

        IllegalArgumentException exception = mock(IllegalArgumentException.class);

        ResponseEntity<Object> responseEntity =
                exceptionMapper.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

    }

    @Test
    public void should_handle_http_message_not_readable_exception() {

        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        ResponseEntity<Object> responseEntity =
                exceptionMapper.httpMessageNotReadableExceptionError(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

    }

    @Test
    public void should_handle_http_media_type_not_supported_exception() {

        IllegalArgumentException exception = mock(IllegalArgumentException.class);

        ResponseEntity<Object> responseEntity =
                exceptionMapper.handleHttpMediaTypeNotSupported(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

    }

    @Test
    public void should_handle_exception() {

        Exception exception = mock(Exception.class);

        ResponseEntity<Object> responseEntity = exceptionMapper.handleException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());

    }
}
