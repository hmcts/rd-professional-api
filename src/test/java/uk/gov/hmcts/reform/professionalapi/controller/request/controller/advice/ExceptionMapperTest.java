package uk.gov.hmcts.reform.professionalapi.controller.request.controller.advice;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExceptionMapper;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionMapperTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private HttpHeaders httpHeaders;

    @Mock
    private HttpStatus httpStatus;

    @InjectMocks
    private ExceptionMapper exceptionMapper;

    @Test
    public void should_handle_empty_result_exception() {

        EmptyResultDataAccessException emptyResultDataAccessException = mock(EmptyResultDataAccessException.class);

        ResponseEntity<Object> responseEntity =
            exceptionMapper.handleEmptyResultDataAccessException(httpServletRequest, emptyResultDataAccessException, httpHeaders, httpStatus);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

    }

    @Test
    public void should_handle_exception() {

        Exception exception = mock(Exception.class);

        ResponseEntity<String> responseEntity = exceptionMapper.handleException(httpServletRequest, exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());

    }
}
