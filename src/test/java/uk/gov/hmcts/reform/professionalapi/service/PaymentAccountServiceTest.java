package uk.gov.hmcts.reform.professionalapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.configuration.ApplicationConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.PaymentAccountServiceImpl;
import uk.gov.hmcts.reform.professionalapi.util.PbaAccountUtil;


public class PaymentAccountServiceTest {

    private final ApplicationConfiguration applicationConfigurationMock = mock(ApplicationConfiguration.class);
    private final ProfessionalUserRepository professionalUserRepositoryMock = mock(ProfessionalUserRepository.class);
    UserProfileFeignClient userProfileFeignClientMock = mock(UserProfileFeignClient.class);

    private final PaymentAccountService sut = new PaymentAccountServiceImpl(applicationConfigurationMock, userProfileFeignClientMock,professionalUserRepositoryMock);

    private Organisation organisationMock;

    @Before
    public void setUp() {
        organisationMock = mock(Organisation.class);
    }

    @Test
    public void retrievePaymentAccountsByPbaEmailWhenConfigTrue() {

        final List<UserAccountMap> userAccountMaps = new ArrayList<>();
        final List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(new PaymentAccount());

        ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);
        PaymentAccount paymentAccountMock = mock(PaymentAccount.class);

        final UUID paymentAccountUuid = UUID.randomUUID();

        UserAccountMapId newUserAccountMapId = new UserAccountMapId(professionalUserMock, paymentAccountMock);

        when(professionalUserMock.getOrganisation()).thenReturn(organisationMock);

        when(applicationConfigurationMock.getPbaFromUserAccountMap()).thenReturn("true");

        when(organisationMock.getStatus()).thenReturn(OrganisationStatus.ACTIVE);

        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);

        when(professionalUserMock.getUserAccountMap()).thenReturn(userAccountMaps);

        when(paymentAccountMock.getId()).thenReturn(paymentAccountUuid);

        List<PaymentAccount> paymentAccounts1 = PbaAccountUtil.getPaymentAccountsFromUserAccountMap(userAccountMaps);

        when(professionalUserRepositoryMock.findByEmailAddress("some-email"))
                .thenReturn(professionalUserMock);

        Organisation organisation = sut.findPaymentAccountsByEmail("some-email");

        assertThat(organisation).isNotNull();

        verify(
                organisationMock,
                times(1)).setUsers(any());

        verify(
                organisationMock,
                times(1)).setPaymentAccounts(any());

    }

    @Test
    public void retrievePaymentAccountsByPbaEmailWhenConfigFalse() {

        final List<UserAccountMap> userAccountMaps = new ArrayList<>();
        final List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(new PaymentAccount());

        ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);
        PaymentAccount paymentAccountMock = mock(PaymentAccount.class);

        final UUID paymentAccountUuid = UUID.randomUUID();

        UserAccountMapId newUserAccountMapId = new UserAccountMapId(professionalUserMock, paymentAccountMock);

        when(professionalUserMock.getOrganisation()).thenReturn(organisationMock);

        when(applicationConfigurationMock.getPbaFromUserAccountMap()).thenReturn("false");

        when(organisationMock.getStatus()).thenReturn(OrganisationStatus.ACTIVE);

        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);

        List<PaymentAccount> paymentAccounts1 = PbaAccountUtil.getPaymentAccount(paymentAccounts);

        when(professionalUserRepositoryMock.findByEmailAddress("some-email"))
                .thenReturn(professionalUserMock);

        Organisation organisation = sut.findPaymentAccountsByEmail("some-email");

        assertThat(organisation).isNotNull();

        verify(
                organisationMock,
                times(1)).setPaymentAccounts(any());

    }

    @Test(expected = Exception.class)
    public void testThrowsExceptionWhenEmailInvalid() {
        when(sut.findPaymentAccountsByEmail("some-email"))
                .thenReturn(organisationMock);
    }
}