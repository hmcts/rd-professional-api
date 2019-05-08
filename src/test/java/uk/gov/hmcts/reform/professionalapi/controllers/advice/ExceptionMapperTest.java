package uk.gov.hmcts.reform.professionalapi.controllers.advice;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExceptionMapper;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionMapperTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private ExceptionMapper exceptionMapper;

    @Test
    public void should_handle_empty_result_exception() {

        EmptyResultDataAccessException emptyResultDataAccessException = mock(EmptyResultDataAccessException.class);

        ResponseEntity<String> responseEntity =
            exceptionMapper.handleEmptyResultDataAccessException(httpServletRequest, emptyResultDataAccessException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());

    }

    @Test
    public void should_handle_exception() {

        Exception exception = mock(Exception.class);

        ResponseEntity<String> responseEntity = exceptionMapper.handleException(httpServletRequest, exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());

    }
}
