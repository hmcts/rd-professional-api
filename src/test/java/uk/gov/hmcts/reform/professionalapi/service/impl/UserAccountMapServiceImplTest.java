package uk.gov.hmcts.reform.professionalapi.service.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.professionalapi.controller.request.InvalidRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.domain.UserAttribute;
import uk.gov.hmcts.reform.professionalapi.repository.UserAccountMapRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
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

    @Test
    void test_updateUser() {
        PaymentAccount pba = new PaymentAccount("PBA1234567");
        ProfessionalUser existingProfessionalUser = new ProfessionalUser("some-fname",
            "some-lname", "test@test.com", new Organisation());

        List<UserAccountMap> userAccountMaps = new ArrayList<>();
        UserAccountMap accMap = new UserAccountMap(
            new UserAccountMapId(existingProfessionalUser, pba));
        userAccountMaps.add(accMap);

        when(userAccountMapRepositoryMock.fetchByProfessionalUserId(
            existingProfessionalUser.getId())).thenReturn(userAccountMaps);

        ProfessionalUser newProfessionalUser = new ProfessionalUser("some-fname",
            "some-lname", "newtest@test.com", new Organisation());
        sut.updateUser(existingProfessionalUser,newProfessionalUser);

        verify(userAccountMapRepositoryMock, Mockito.times(1)).save(any());
        verify(userAccountMapRepositoryMock, Mockito.times(1)).delete(any());

    }
}