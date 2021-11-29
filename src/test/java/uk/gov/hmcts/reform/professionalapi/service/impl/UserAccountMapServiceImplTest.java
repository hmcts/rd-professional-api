package uk.gov.hmcts.reform.professionalapi.service.impl;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.repository.UserAccountMapRepository;

class UserAccountMapServiceImplTest {

    private final UserAccountMapRepository userAccountMapRepositoryMock = mock(UserAccountMapRepository.class);
    private final UserAccountMapServiceImpl sut =
            new UserAccountMapServiceImpl(userAccountMapRepositoryMock);
    private final ProfessionalUser persistedSuperUser = new ProfessionalUser();
    private final List<PaymentAccount> paymentAccounts = new ArrayList<>();

    @Test
    void test_persistedUserAccountMap() {
        PaymentAccount pba = new PaymentAccount("PBA1234567");
        paymentAccounts.add(0, pba);

        sut.persistedUserAccountMap(persistedSuperUser, paymentAccounts);

        verify(userAccountMapRepositoryMock, times(1)).saveAll(anyList());
    }
}