package uk.gov.hmcts.reform.professionalapi.service.impl;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.repository.UserAccountMapRepository;
import uk.gov.hmcts.reform.professionalapi.service.UserAccountMapService;

@Service
@Slf4j
public class UserAccountMapServiceImpl implements UserAccountMapService {

    private UserAccountMapRepository userAccountMapRepository;

    public UserAccountMapServiceImpl(UserAccountMapRepository userAccountMapRepository) {
        this.userAccountMapRepository = userAccountMapRepository;
    }

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    public void persistedUserAccountMap(ProfessionalUser persistedSuperUser, List<PaymentAccount> paymentAccounts) {

        if (!paymentAccounts.isEmpty()) {
            List<UserAccountMap> userAccountMaps = new ArrayList<>();
            log.debug("{}:: PaymentAccount is not empty", loggingComponentName);
            paymentAccounts.forEach(paymentAccount ->
                    userAccountMaps.add(new UserAccountMap(new UserAccountMapId(persistedSuperUser, paymentAccount))));
            if (!CollectionUtils.isEmpty(userAccountMaps)) {
                userAccountMapRepository.saveAll(userAccountMaps);
            }
        }
    }
}
