package uk.gov.hmcts.reform.professionalapi.service.impl;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.professionalapi.configuration.ApplicationConfiguration;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.service.LegacyPbaAccountService;



public class LegacyPbaAccountServiceImplTest {

    List<String> paymentAccountPbaNumbers = new ArrayList<>();

    ProfessionalUser professionalUserMock;

    ApplicationConfiguration configurationMock;

    Organisation organisationMock;

    @InjectMocks
    LegacyPbaAccountServiceImpl sut;

    @Before
    public void setup() {

        professionalUserMock = mock(ProfessionalUser.class);
        configurationMock = mock(ApplicationConfiguration.class);
        organisationMock = mock(Organisation.class);
        final List<UserAccountMap> userAccountMap = new ArrayList<>();
        /*final List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(new PaymentAccount("PBA123"));*/

        /*when(configurationMock.getPbaFromUserAccountMap()).thenReturn("false");
        when(professionalUserMock.getOrganisation()).thenReturn(organisationMock);
        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);*/
        when(professionalUserMock.getUserAccountMap()).thenReturn(userAccountMap);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindLegacyAccountByUserEmailWhenPbaIsEmpty() {

        final List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(new PaymentAccount("PBA123"));
        when(configurationMock.getPbaFromUserAccountMap()).thenReturn("false");
        when(professionalUserMock.getOrganisation()).thenReturn(organisationMock);
        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);

        paymentAccountPbaNumbers = sut.findLegacyPbaAccountByUserEmail(professionalUserMock);
        assertThat(paymentAccountPbaNumbers).isNotNull();
        assertThat(paymentAccountPbaNumbers.size()).isEqualTo(1);
    }

    @Ignore
    @Test
    public void testFindLegacyAccountByUserEmail() throws Exception {

        ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);
        ApplicationConfiguration configurationMock = mock(ApplicationConfiguration.class);
        when(configurationMock.getPbaFromUserAccountMap()).thenReturn("true");

        List<UserAccountMap> userAccountMapData = new ArrayList<>();
        PaymentAccount paymentAccountMock = mock(PaymentAccount.class);
        UserAccountMapId newUserAccountMapId = new UserAccountMapId(professionalUserMock, paymentAccountMock);
        UserAccountMap userAccountMap = new UserAccountMap(newUserAccountMapId);
        final UUID paymentAccountUuid = UUID.randomUUID();

        Field f = userAccountMap.getClass().getDeclaredField("userAccountMapId");
        f.setAccessible(true);
        f.set(userAccountMap, newUserAccountMapId);

        userAccountMapData.add(userAccountMap);

        List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(paymentAccountMock);

        Organisation organisationMock = mock(Organisation.class);
        Organisation organisation = new Organisation();
        organisation.setPaymentAccounts(paymentAccounts);

        when(professionalUserMock.getOrganisation()).thenReturn(organisationMock);

        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);

        when(professionalUserMock.getUserAccountMap()).thenReturn(userAccountMapData);

        when(paymentAccountMock.getId()).thenReturn(paymentAccountUuid);

        paymentAccountPbaNumbers = sut.findLegacyPbaAccountByUserEmail(professionalUserMock);

        assertThat(paymentAccountPbaNumbers).isNotNull();


    }
}