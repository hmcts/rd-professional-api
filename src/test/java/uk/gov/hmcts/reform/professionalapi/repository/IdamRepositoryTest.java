package uk.gov.hmcts.reform.professionalapi.repository;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

public class IdamRepositoryTest {

    @Mock
    private IdamClient idamClient;

    @InjectMocks
    private IdamRepository idamRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void test_getUserInfo() {
        UserInfo userInfo = mock(UserInfo.class);
        Mockito.when(idamClient.getUserInfo(anyString())).thenReturn(userInfo);
        UserInfo returnedUserInfo = idamRepository.getUserInfo("Test");
        assertNotNull(returnedUserInfo);
    }
}
