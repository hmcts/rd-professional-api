package uk.gov.hmcts.reform.professionalapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.configuration.ApplicationConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.domain.*;
import uk.gov.hmcts.reform.professionalapi.persistence.*;
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
    private final UserAccountMapService userAccountMapServiceMock = mock(UserAccountMapService.class);
    private final DxAddressRepository dxAddressRepositoryMock = mock(DxAddressRepository.class);
    private final ContactInformationRepository contactInformationRepositoryMock = mock(ContactInformationRepository.class);
    private final PrdEnumRepository prdEnumRepositoryMock = mock(PrdEnumRepository.class);
    private final UserProfileFeignClient userProfileFeignClient = mock(UserProfileFeignClient.class);
    private final PrdEnumService prdEnumServiceMock = mock(PrdEnumService.class);
    private final UserAttributeService userAttributeServiceMock = mock(UserAttributeService.class);
    private final UserAccountMap userAccountMapMock = mock(UserAccountMap.class);
    private final List<UserAccountMap> userAccountMaps = new ArrayList<>();

    private final OrganisationServiceImpl organisationServiceMock = new OrganisationServiceImpl(
            organisationRepositoryMock, professionalUserRepositoryMock, paymentAccountRepositoryMock,
            dxAddressRepositoryMock, contactInformationRepositoryMock, prdEnumRepositoryMock,
            userAccountMapServiceMock, userProfileFeignClient, prdEnumServiceMock,
            userAttributeServiceMock);

    private final PaymentAccountServiceImpl sut = new PaymentAccountServiceImpl(
            applicationConfigurationMock, userProfileFeignClientMock, professionalUserRepositoryMock,
            paymentAccountRepositoryMock, organisationServiceMock, userAccountMapServiceMock);

    private final SuperUser superUserMock = mock(SuperUser.class);
    private final PaymentAccount paymentAccountMock = mock(PaymentAccount.class);
    private final ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);
    private Organisation organisationMock;
    private List<SuperUser> superUsers = new ArrayList<>();
    private List<PaymentAccount> paymentAccounts = new ArrayList<>();
    private Set<String> pbas = new HashSet<>();
    private PbaEditRequest pbaEditRequest = new PbaEditRequest(null);

    @Before
    public void setUp() {
        organisationMock = mock(Organisation.class);
        superUsers.add(superUserMock);
        paymentAccounts.add(paymentAccountMock);
        pbas.add("PBA0000001");
        pbaEditRequest.setPaymentAccounts(pbas);
        userAccountMaps.add(userAccountMapMock);
    }

    @Test
    public void retrievePaymentAccountsByPbaEmailWhenConfigTrue() {
        final List<UserAccountMap> userAccountMaps = new ArrayList<>();
        final List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(new PaymentAccount());

        ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);
        PaymentAccount paymentAccountMock = mock(PaymentAccount.class);

        final UUID paymentAccountUuid = UUID.randomUUID();

        when(professionalUserMock.getOrganisation()).thenReturn(organisationMock);
        when(applicationConfigurationMock.getPbaFromUserAccountMap()).thenReturn("true");
        when(organisationMock.getStatus()).thenReturn(OrganisationStatus.ACTIVE);
        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);
        when(professionalUserMock.getUserAccountMap()).thenReturn(userAccountMaps);
        when(paymentAccountMock.getId()).thenReturn(paymentAccountUuid);
        when(professionalUserRepositoryMock.findByEmailAddress("some-email")).thenReturn(professionalUserMock);

        RefDataUtil.getPaymentAccountsFromUserAccountMap(userAccountMaps);

        Organisation organisation = sut.findPaymentAccountsByEmail("some-email");
        assertThat(organisation).isNotNull();

        verify(organisationMock, times(1)).setUsers(any());
        verify(organisationMock, times(1)).setPaymentAccounts(any());
    }

    @Test
    public void retrievePaymentAccountsByPbaEmailWhenConfigFalse() {
        final List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(new PaymentAccount());
        ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);

        when(professionalUserMock.getOrganisation()).thenReturn(organisationMock);
        when(applicationConfigurationMock.getPbaFromUserAccountMap()).thenReturn("false");
        when(organisationMock.getStatus()).thenReturn(OrganisationStatus.ACTIVE);
        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);
        when(professionalUserRepositoryMock.findByEmailAddress("some-email")).thenReturn(professionalUserMock);

        RefDataUtil.getPaymentAccount(paymentAccounts);

        Organisation organisation = sut.findPaymentAccountsByEmail("some-email");
        assertThat(organisation).isNotNull();

        verify(organisationMock, times(1)).setPaymentAccounts(any());
        verify(organisationMock, times(1)).setUsers(any());
        verify(organisationMock, times(1)).setPaymentAccounts(any());

    }

    @Test(expected = Exception.class)
    public void testThrowsExceptionWhenEmailInvalid() {
        when(sut.findPaymentAccountsByEmail("some-email")).thenReturn(organisationMock);
    }

    @Test
    public void deleteUserAndPaymentAccountsFromUserAccountMapTest() {

        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisationMock);
        when(organisationMock.getOrganisationIdentifier()).thenReturn("AK57L4T");
        when(organisationMock.getUsers()).thenReturn(superUsers);
        when(organisationMock.getUsers().get(0).toProfessionalUser()).thenReturn(professionalUserMock);
        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);

        sut.deleteUserAccountMaps(organisationMock);

        verify(userAccountMapServiceMock, times(1)).deleteByUserAccountMapIdIn(anyList());
    }

    @Test
    public void deletePaymentAccountsFromOrganisationTest() {

        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisationMock);
        when(organisationMock.getOrganisationIdentifier()).thenReturn("AK57L4T");
        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);
        when(paymentAccountMock.getId()).thenReturn(UUID.randomUUID());

        sut.deletePaymentAccountsFromOrganisation(organisationMock);

        verify(paymentAccountRepositoryMock, times(1)).deleteByIdIn(anyList());
        verify(organisationMock, times(1)).getPaymentAccounts();
        verify(organisationMock, times(1)).setPaymentAccounts(anyList());
    }

    @Test
    public void addPaymentAccountsToOrganisationTest() {
        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisationMock);
        when(organisationMock.getOrganisationIdentifier()).thenReturn("AK57L4T");

        sut.addPaymentAccountsToOrganisation(pbaEditRequest, organisationMock);

        verify(paymentAccountRepositoryMock, times(1)).save(any(PaymentAccount.class));
    }

    @Test
    public void addUserAndPaymentAccountsToUserAccountMapTest() {
        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisationMock);
        when(organisationMock.getOrganisationIdentifier()).thenReturn("AK57L4T");
        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);

        when(organisationMock.getUsers()).thenReturn(superUsers);
        when(organisationMock.getUsers().get(0).toProfessionalUser()).thenReturn(professionalUserMock);

        sut.addUserAndPaymentAccountsToUserAccountMap(organisationMock);

        verify(userAccountMapServiceMock, times(1)).persistedUserAccountMap(any(ProfessionalUser.class), anyList());

        assertThat(sut.addUserAndPaymentAccountsToUserAccountMap(organisationMock)).isNotNull();
    }

    @Test
    public void generateListOfAccountsToDelete() {
        ProfessionalUser prefU = new ProfessionalUser("Con","Hal","email@gmail.com",organisationMock);
        List<UserAccountMapId> listUserMap = new ArrayList<>();
        assertThat(sut.generateListOfAccountsToDelete(prefU,paymentAccounts)).isNotNull();
        listUserMap = sut.generateListOfAccountsToDelete(prefU,paymentAccounts);
        assertThat(listUserMap.get(0).getProfessionalUser().getFirstName()).isEqualTo("Con");
    }
}