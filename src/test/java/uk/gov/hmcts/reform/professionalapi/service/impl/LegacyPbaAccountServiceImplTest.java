package uk.gov.hmcts.reform.professionalapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

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

    ApplicationConfiguration configurationMock;

    private Organisation organisation;
    private ProfessionalUser professionalUser;
    private PaymentAccount paymentAccount;

    List<String> paymentAccountPbaNumbers = new ArrayList<>();
    List<PaymentAccount> paymentAccounts = new ArrayList<>();
    List<UserAccountMap> userAccountMap = new ArrayList<>();

    @InjectMocks
    LegacyPbaAccountServiceImpl sut;

    @Before
    public void setup() {
        configurationMock = mock(ApplicationConfiguration.class);
        organisation = new Organisation("some-org-name", null, "PENDING", null, null, null);
        paymentAccounts.add(new PaymentAccount("PBA123"));
        organisation.setPaymentAccounts(paymentAccounts);
        professionalUser = new ProfessionalUser("some-fname", "some-lname", "some@hmcts.net", organisation);
        organisation.setPaymentAccounts(paymentAccounts);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testFindLegacyAccountByUserEmailWhenPbaIsEmpty() {
        when(configurationMock.getPbaFromUserAccountMap()).thenReturn("false");

        paymentAccountPbaNumbers = sut.findLegacyPbaAccountByUserEmail(professionalUser);
        assertThat(paymentAccountPbaNumbers).isNotNull();
        assertThat(paymentAccountPbaNumbers.size()).isEqualTo(1);
    }

    @Test
    public void testFindLegacyAccountByUserEmail() throws Exception {
        when(configurationMock.getPbaFromUserAccountMap()).thenReturn("true");
        List<UserAccountMap> userAccountMapData = new ArrayList<>();
        UserAccountMapId newUserAccountMapId = new UserAccountMapId(professionalUser, paymentAccount);
        UserAccountMap userAccountMap = new UserAccountMap(newUserAccountMapId);
        userAccountMapData.add(userAccountMap);


        paymentAccountPbaNumbers = sut.findLegacyPbaAccountByUserEmail(professionalUser);

        assertThat(paymentAccountPbaNumbers).isNotNull();
        assertThat(paymentAccountPbaNumbers.size()).isGreaterThanOrEqualTo(0);
    }
}