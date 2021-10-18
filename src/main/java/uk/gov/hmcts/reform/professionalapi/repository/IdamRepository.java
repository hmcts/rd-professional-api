package uk.gov.hmcts.reform.professionalapi.repository;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static java.util.Objects.requireNonNull;

@Component
@Slf4j
public class IdamRepository {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    private final IdamClient idamClient;
    private CacheManager cacheManager;

    @Autowired
    public IdamRepository(IdamClient idamClient, CacheManager cacheManager) {
        this.idamClient = idamClient;
        this.cacheManager = cacheManager;
    }

    @Cacheable(value = "token")
    public UserInfo getUserInfo(String jwtToken) {
        CaffeineCache caffeineCache = (CaffeineCache) cacheManager.getCache("token");
        Cache<Object, Object> nativeCache = requireNonNull(caffeineCache).getNativeCache();

        log.info("{}:: generating Bearer Token, current size of cache: {}",
                loggingComponentName, nativeCache.estimatedSize());

        return idamClient.getUserInfo("Bearer " + jwtToken);
    }

}
