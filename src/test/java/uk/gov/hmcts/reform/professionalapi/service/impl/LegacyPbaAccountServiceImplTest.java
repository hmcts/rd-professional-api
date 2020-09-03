package uk.gov.hmcts.reform.professionalapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

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


public class LegacyPbaAccountServiceImplTest {

    ApplicationConfiguration configurationMock;

    private Organisation organisation;
    private ProfessionalUser professionalUser;

    List<String> paymentAccountPbaNumbers = new ArrayList<>();
    List<PaymentAccount> paymentAccounts = new ArrayList<>();

    @InjectMocks
    LegacyPbaAccountServiceImpl sut;

    @Before
    public void setup() {
        configurationMock = mock(ApplicationConfiguration.class);
        organisation = new Organisation("some-org-name", null, "PENDING", null, null, null);
        paymentAccounts.add(new PaymentAccount("PBA1234567"));
        organisation.setPaymentAccounts(paymentAccounts);
        professionalUser = new ProfessionalUser("some-fname", "some-lname", "some@hmcts.net", organisation);
        organisation.setPaymentAccounts(paymentAccounts);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindLegacyAccountByUserEmailWhenPbaIsEmpty() {
        paymentAccountPbaNumbers = sut.findLegacyPbaAccountByUserEmail(professionalUser);
        assertThat(paymentAccountPbaNumbers).isNotNull();
        assertThat(paymentAccountPbaNumbers.size()).isEqualTo(1);
        assertThat(paymentAccountPbaNumbers.get(0)).isEqualTo("PBA1234567");
    }
}