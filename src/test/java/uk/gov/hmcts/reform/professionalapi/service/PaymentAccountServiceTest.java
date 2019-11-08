package uk.gov.hmcts.reform.professionalapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import org.springframework.dao.EmptyResultDataAccessException;
import uk.gov.hmcts.reform.professionalapi.configuration.ApplicationConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.domain.*;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAccountMapRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.PaymentAccountServiceImpl;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;


public class PaymentAccountServiceTest {

    private final ApplicationConfiguration applicationConfigurationMock = mock(ApplicationConfiguration.class);
    private final ProfessionalUserRepository professionalUserRepositoryMock = mock(ProfessionalUserRepository.class);
    private final UserProfileFeignClient userProfileFeignClientMock = mock(UserProfileFeignClient.class);
    private final PaymentAccountRepository paymentAccountRepositoryMock = mock(PaymentAccountRepository.class);
    private final OrganisationRepository organisationRepositoryMock = mock(OrganisationRepository.class);
    private final UserAccountMapRepository userAccountMapRepositoryMock = mock(UserAccountMapRepository.class);
    private final OrganisationServiceImpl organisationServiceMock = mock(OrganisationServiceImpl.class);
    private final SuperUser superUserMock = mock(SuperUser.class);
    private final PaymentAccount paymentAccountMock = mock(PaymentAccount.class);
    private final ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);

    private final PaymentAccountServiceImpl sut = new PaymentAccountServiceImpl(applicationConfigurationMock, userProfileFeignClientMock, professionalUserRepositoryMock, paymentAccountRepositoryMock, organisationRepositoryMock, userAccountMapRepositoryMock, organisationServiceMock);

    private Organisation organisationMock;
    private List<SuperUser> superUsers = new ArrayList<>();
    private List<PaymentAccount> paymentAccounts = new ArrayList<>();
    private List<String> pbas = new ArrayList<>();
    private PbaEditRequest pbaEditRequest = new PbaEditRequest(pbas);

    @Before
    public void setUp() {
        organisationMock = mock(Organisation.class);

        superUsers.add(superUserMock);

        paymentAccounts.add(paymentAccountMock);

        pbas.add("PBA0000001");

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

        List<PaymentAccount> paymentAccounts1 = RefDataUtil.getPaymentAccountsFromUserAccountMap(userAccountMaps);

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

        List<PaymentAccount> paymentAccounts1 = RefDataUtil.getPaymentAccount(paymentAccounts);

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

    @Test
    public void editPaymentsAccountsByOrgId() {
        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisationMock);
        when(organisationMock.getOrganisationIdentifier()).thenReturn("AK57L4T");

        //delete user and payment account code:
        when(organisationMock.getUsers()).thenReturn(superUsers);
        when(organisationMock.getUsers().get(0).toProfessionalUser()).thenReturn(professionalUserMock);
        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);

        //delete payment account from org code:
        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);
        when(paymentAccountMock.getId()).thenReturn(UUID.randomUUID());

        PbaResponse pbaResponse = sut.editPaymentsAccountsByOrgId(pbaEditRequest, organisationMock.getOrganisationIdentifier());

        assertThat(pbaResponse.getStatusMessage()).isEqualTo("Success");
        assertThat(pbaResponse.getStatusCode()).isEqualTo("200");
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void editPaymentsAccountsByOrgIdThrows404() {
        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(null);

        sut.editPaymentsAccountsByOrgId(pbaEditRequest, organisationMock.getOrganisationIdentifier());

    }
}