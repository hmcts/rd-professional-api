package uk.gov.hmcts.reform.sysrefdataapi.infrastructure.controllers.advice;

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
import uk.gov.hmcts.reform.sysrefdataapi.domain.RequiredFieldMissingException;

@RunWith(MockitoJUnitRunner.class)
public class SysRefDataAdviceTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private SysRefDataAdvice sysRefDataAdvice;

    @Test
    public void should_handle_required_field_exception() {

        RequiredFieldMissingException requiredFieldMissingException = mock(RequiredFieldMissingException.class);

        ResponseEntity<String> responseEntity =
            sysRefDataAdvice.handleRequiredFieldMissingException(httpServletRequest, requiredFieldMissingException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

    }

    @Test
    public void should_handle_empty_result_exception() {

        EmptyResultDataAccessException emptyResultDataAccessException = mock(EmptyResultDataAccessException.class);

        ResponseEntity<String> responseEntity =
            sysRefDataAdvice.handleEmptyResultDataAccessException(httpServletRequest, emptyResultDataAccessException);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());

    }

    @Test
    public void should_handle_exception() {

        Exception exception = mock(Exception.class);

        ResponseEntity<String> responseEntity = sysRefDataAdvice.handleException(httpServletRequest, exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());

    }
}
