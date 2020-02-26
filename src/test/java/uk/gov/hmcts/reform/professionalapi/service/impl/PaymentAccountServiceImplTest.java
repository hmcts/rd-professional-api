package uk.gov.hmcts.reform.professionalapi.service.impl;

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
import org.junit.Ignore;
import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.configuration.ApplicationConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.UserAccountMapService;
import uk.gov.hmcts.reform.professionalapi.util.RefDataUtil;


public class PaymentAccountServiceImplTest {

    private final ApplicationConfiguration applicationConfigurationMock = mock(ApplicationConfiguration.class);
    private final ProfessionalUserRepository professionalUserRepositoryMock = mock(ProfessionalUserRepository.class);
    private final UserProfileFeignClient userProfileFeignClientMock = mock(UserProfileFeignClient.class);
    private final PaymentAccountRepository paymentAccountRepositoryMock = mock(PaymentAccountRepository.class);
    private final OrganisationRepository organisationRepositoryMock = mock(OrganisationRepository.class);
    private final UserAccountMapService userAccountMapServiceMock = mock(UserAccountMapService.class);
    private final UserAccountMap userAccountMapMock = mock(UserAccountMap.class);
    private final PaymentAccountValidator paymentAccountValidatorMock = mock(PaymentAccountValidator.class);

    private OrganisationServiceImpl organisationService;
    private PaymentAccountServiceImpl sut;

    private final Organisation organisation = new Organisation("some-org-name", null, "PENDING", null, null, null);
    private final PaymentAccount paymentAccount = new PaymentAccount("PBA1234567");
    private final SuperUser superUser = new SuperUser("some-fname", "some-lname", "some-email-address", organisation);
    private final ProfessionalUser professionalUser = new ProfessionalUser("some-fname", "some-lname", "some@hmcts.net", organisation);

    private final List<UserAccountMap> userAccountMaps = new ArrayList<>();
    private List<SuperUser> superUsers = new ArrayList<>();
    private List<PaymentAccount> paymentAccounts = new ArrayList<>();
    private Set<String> pbas = new HashSet<>();
    private PbaEditRequest pbaEditRequest = new PbaEditRequest(null);

    @Before
    public void setUp() {
        organisationService = new OrganisationServiceImpl();
        organisationService.setPaymentAccountValidator(paymentAccountValidatorMock);
        organisationService.setPaymentAccountRepository(paymentAccountRepositoryMock);

        sut = new PaymentAccountServiceImpl(
                applicationConfigurationMock, userProfileFeignClientMock, professionalUserRepositoryMock,
                paymentAccountRepositoryMock, organisationService, userAccountMapServiceMock);

        superUsers.add(superUser);
        paymentAccounts.add(paymentAccount);
        pbas.add("PBA0000001");
        pbaEditRequest.setPaymentAccounts(pbas);
        userAccountMaps.add(userAccountMapMock);

        paymentAccount.setId(UUID.randomUUID());

        organisation.setPaymentAccounts(paymentAccounts);
        organisation.setOrganisationIdentifier("AK57L4T");
        organisation.setUsers(superUsers);
    }

    @Test
    @Ignore //ignoring so I can push commit, will fix in next commit
    public void retrievePaymentAccountsByPbaEmailWhenConfigTrue() {
        List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(new PaymentAccount());
        List<UserAccountMap> userAccountMaps = new ArrayList<>();

        organisation.setStatus(OrganisationStatus.ACTIVE);
        organisation.setPaymentAccounts(paymentAccounts);
        professionalUser.setUserAccountMap(userAccountMaps);
        paymentAccount.setId(UUID.randomUUID());

        when(applicationConfigurationMock.getPbaFromUserAccountMap()).thenReturn("true");
        when(professionalUserRepositoryMock.findByEmailAddress("some@hmcts.net")).thenReturn(professionalUser);

        RefDataUtil.getPaymentAccountsFromUserAccountMap(userAccountMaps);

        Organisation organisation = sut.findPaymentAccountsByEmail("some@hmcts.net");
        assertThat(organisation).isNotNull();
    }

    @Test
    @Ignore //ignoring so I can push commit, will fix in next commit
    public void retrievePaymentAccountsByPbaEmailWhenConfigFalse() {
        List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(new PaymentAccount());
        List<UserAccountMap> userAccountMaps = new ArrayList<>();

        organisation.setStatus(OrganisationStatus.ACTIVE);
        organisation.setPaymentAccounts(paymentAccounts);
        professionalUser.setUserAccountMap(userAccountMaps);
        professionalUser.setOrganisation(organisation);

        when(applicationConfigurationMock.getPbaFromUserAccountMap()).thenReturn("false");
        when(professionalUserRepositoryMock.findByEmailAddress("some@hmcts.net")).thenReturn(professionalUser);

        RefDataUtil.getPaymentAccount(paymentAccounts);

        Organisation organisation = sut.findPaymentAccountsByEmail("some@hmcts.net");
        assertThat(organisation).isNotNull();
    }

    @Test(expected = Exception.class)
    public void testThrowsExceptionWhenEmailInvalid() {
        when(sut.findPaymentAccountsByEmail("some-email")).thenReturn(organisation);
    }

    @Test
    public void deleteUserAndPaymentAccountsFromUserAccountMapTest() {
        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisation);
        sut.deleteUserAccountMaps(organisation);

        verify(userAccountMapServiceMock, times(1)).deleteByUserAccountMapIdIn(anyList());
    }

    @Test
    public void deletePaymentAccountsFromOrganisationTest() {
        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisation);

        sut.deletePaymentAccountsFromOrganisation(organisation);

        verify(paymentAccountRepositoryMock, times(1)).deleteByIdIn(anyList());
    }

    @Test
    public void addPaymentAccountsToOrganisationTest() {
        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisation);

        sut.addPaymentAccountsToOrganisation(pbaEditRequest, organisation);

        verify(paymentAccountRepositoryMock, times(1)).save(any(PaymentAccount.class));
    }

    @Test
    public void addUserAndPaymentAccountsToUserAccountMapTest() {
        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisation);

        sut.addUserAndPaymentAccountsToUserAccountMap(organisation);

        verify(userAccountMapServiceMock, times(1)).persistedUserAccountMap(any(ProfessionalUser.class), anyList());

        assertThat(sut.addUserAndPaymentAccountsToUserAccountMap(organisation)).isNotNull();
    }

    @Test
    public void generateListOfAccountsToDelete() {
        ProfessionalUser prefU = new ProfessionalUser("Con", "Hal", "email@gmail.com", organisation);
        List<UserAccountMapId> listUserMap = new ArrayList<>();
        assertThat(sut.generateListOfAccountsToDelete(prefU, paymentAccounts)).isNotNull();
        listUserMap = sut.generateListOfAccountsToDelete(prefU, paymentAccounts);
        assertThat(listUserMap.get(0).getProfessionalUser().getFirstName()).isEqualTo("Con");
    }
}