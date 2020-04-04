package uk.gov.hmcts.reform.professionalapi.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

@Component
@Slf4j
public class IdamRepository {

    private final IdamClient idamClient;

    @Autowired
    public IdamRepository(IdamClient idamClient) {
        this.idamClient = idamClient;
    }

    @Cacheable(value = "userInfoCache")
    public UserInfo getUserInfo(String jwtToken) {
        log.info("Inside getUserInfo:::");
        return idamClient.getUserInfo("Bearer " + jwtToken);
    }

}
