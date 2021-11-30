package uk.gov.hmcts.reform.professionalapi.repository;

import com.github.benmanes.caffeine.cache.Cache;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdamRepositoryTest {

    @Mock
    private IdamClient idamClient;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private IdamRepository idamRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @SuppressWarnings("unchecked")
    void test_getUserInfo() {
        UserInfo userInfo = mock(UserInfo.class);
        CaffeineCache caffeineCacheMock = mock(CaffeineCache.class);
        Cache cache = mock(Cache.class);

        when(idamClient.getUserInfo(anyString())).thenReturn(userInfo);
        when(cacheManager.getCache(anyString())).thenReturn(caffeineCacheMock);
        when(caffeineCacheMock.getNativeCache()).thenReturn(cache);
        when(cache.estimatedSize()).thenReturn(anyLong());

        UserInfo returnedUserInfo = idamRepository.getUserInfo("Test");

        assertNotNull(returnedUserInfo);
        verify(idamClient, times(1)).getUserInfo(any());
        verify(cacheManager, times(1)).getCache(any());
        verify(caffeineCacheMock, times(1)).getNativeCache();
        verify(cache, times(1)).estimatedSize();
    }
}