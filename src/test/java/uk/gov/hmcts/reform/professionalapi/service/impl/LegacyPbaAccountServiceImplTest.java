package uk.gov.hmcts.reform.professionalapi.service.impl;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.professionalapi.configuration.ApplicationConfiguration;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;


public class LegacyPbaAccountServiceImplTest {

    List<String> paymentAccountPbaNumbers = new ArrayList<>();
    ProfessionalUser professionalUserMock;
    ApplicationConfiguration configurationMock;
    Organisation organisationMock;
    List<PaymentAccount> paymentAccounts;
    List<UserAccountMap> userAccountMap;
    PaymentAccount paymentAccountMock;

    @InjectMocks
    LegacyPbaAccountServiceImpl sut;

    @Before
    public void setup() {
        professionalUserMock = mock(ProfessionalUser.class);
        configurationMock = mock(ApplicationConfiguration.class);
        organisationMock = mock(Organisation.class);
        paymentAccountMock = mock(PaymentAccount.class);
        userAccountMap = new ArrayList<>();
        paymentAccounts = new ArrayList<>();
        paymentAccounts.add(new PaymentAccount("PBA123"));
        final UUID paymentAccountUuid = UUID.randomUUID();
        when(paymentAccountMock.getId()).thenReturn(paymentAccountUuid);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindLegacyAccountByUserEmailWhenPbaIsEmpty() {
        when(configurationMock.getPbaFromUserAccountMap()).thenReturn("false");
        when(professionalUserMock.getOrganisation()).thenReturn(organisationMock);
        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);
        paymentAccountPbaNumbers = sut.findLegacyPbaAccountByUserEmail(professionalUserMock);
        assertThat(paymentAccountPbaNumbers).isNotNull();
        assertThat(paymentAccountPbaNumbers.size()).isEqualTo(1);
    }

    @Test
    public void testFindLegacyAccountByUserEmail() throws Exception {
        List<UserAccountMap> userAccountMapData = new ArrayList<>();
        UserAccountMapId newUserAccountMapId = new UserAccountMapId(professionalUserMock, paymentAccountMock);
        UserAccountMap userAccountMap = new UserAccountMap(newUserAccountMapId);

        Field f = userAccountMap.getClass().getDeclaredField("userAccountMapId");
        f.setAccessible(true);
        f.set(userAccountMap, newUserAccountMapId);
        userAccountMapData.add(userAccountMap);

        organisationMock.setPaymentAccounts(paymentAccounts);

        when(configurationMock.getPbaFromUserAccountMap()).thenReturn("true");
        when(professionalUserMock.getOrganisation()).thenReturn(organisationMock);
        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);
        when(professionalUserMock.getUserAccountMap()).thenReturn(userAccountMapData);

        paymentAccountPbaNumbers = sut.findLegacyPbaAccountByUserEmail(professionalUserMock);

        assertThat(paymentAccountPbaNumbers).isNotNull();
        assertThat(paymentAccountPbaNumbers.size()).isGreaterThanOrEqualTo(0);
    }
}