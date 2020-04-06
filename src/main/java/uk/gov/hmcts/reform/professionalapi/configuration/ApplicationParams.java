package uk.gov.hmcts.reform.professionalapi.configuration;

import com.hazelcast.config.EvictionPolicy;

import javax.inject.Named;
import javax.inject.Singleton;
import org.springframework.beans.factory.annotation.Value;

@Named
@Singleton
public class ApplicationParams {

    @Value("${definition.cache.max-idle.secs}")
    private Integer definitionCacheMaxIdleSecs;

    @Value("${definition.cache.max.size}")
    private Integer definitionCacheMaxSize;

    @Value("${definition.cache.eviction.policy}")
    private EvictionPolicy definitionCacheEvictionPolicy;

    @Value("${user.cache.ttl.secs}")
    private Integer userCacheTtlSecs;

    public int getDefinitionCacheMaxIdleSecs() {
        return definitionCacheMaxIdleSecs;
    }

    public Integer getUserCacheTtlSecs() {
        return userCacheTtlSecs;
    }

    public int getDefinitionCacheMaxSize() {
        return definitionCacheMaxSize;
    }

    public EvictionPolicy getDefinitionCacheEvictionPolicy() {
        return definitionCacheEvictionPolicy;
    }


}
