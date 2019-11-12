package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAccountMapRepository;
import uk.gov.hmcts.reform.professionalapi.service.UserAccountMapService;

@Service
@Slf4j
@AllArgsConstructor
public class UserAccountMapServiceImpl implements UserAccountMapService {

    private UserAccountMapRepository userAccountMapRepository;

    @Transactional
    public void deleteUserAccountMapsByIds(List<UserAccountMapId> accountsToDelete) {
        userAccountMapRepository.deleteByUserAccountMapIdIn(accountsToDelete);
    }
}
