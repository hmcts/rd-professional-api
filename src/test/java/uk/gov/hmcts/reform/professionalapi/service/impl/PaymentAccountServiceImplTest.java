package uk.gov.hmcts.reform.professionalapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentMatchers;
import uk.gov.hmcts.reform.professionalapi.configuration.ApplicationConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMapId;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
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
    private final UserAccountMapId userAccountMapIdMock = mock(UserAccountMapId.class);
    private final PaymentAccountValidator paymentAccountValidatorMock = mock(PaymentAccountValidator.class);
    private final PaymentAccount paymentAccountMock = mock(PaymentAccount.class);
    private final EntityManagerFactory entityManagerFactoryMock =  mock(EntityManagerFactory.class);
    private final EntityManager entityManagerMock = mock(EntityManager.class);
    private final EntityTransaction entityTransactionMock = mock(EntityTransaction.class);

    private OrganisationServiceImpl organisationService;
    private PaymentAccountServiceImpl sut;

    private final Organisation organisation = new Organisation("some-org-name", null, "PENDING",
            null, null, null);
    private final PaymentAccount paymentAccount = new PaymentAccount("PBA1234567");
    private final SuperUser superUser = new SuperUser("some-fname", "some-lname",
            "some-email-address", organisation);

    private final List<UserAccountMap> userAccountMaps = new ArrayList<>();

    private List<SuperUser> superUsers = new ArrayList<>();
    private List<PaymentAccount> paymentAccounts = new ArrayList<>();
    private Set<String> pbas = new HashSet<>();
    private PbaEditRequest pbaEditRequest = new PbaEditRequest();

    @Before
    public void setUp() {
        organisationService = new OrganisationServiceImpl();
        organisationService.setPaymentAccountValidator(paymentAccountValidatorMock);
        organisationService.setPaymentAccountRepository(paymentAccountRepositoryMock);

        sut = new PaymentAccountServiceImpl(
                applicationConfigurationMock, userProfileFeignClientMock, entityManagerFactoryMock,
                professionalUserRepositoryMock, organisationService, userAccountMapServiceMock);

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
    public void testRetrievePaymentAccountsByPbaEmail() {
        Organisation organisationMock = mock(Organisation.class);
        final List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(new PaymentAccount());
        ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);

        when(professionalUserMock.getOrganisation()).thenReturn(organisationMock);
        when(organisationMock.getStatus()).thenReturn(OrganisationStatus.ACTIVE);
        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);
        when(professionalUserRepositoryMock.findByEmailAddress("some-email")).thenReturn(professionalUserMock);

        RefDataUtil.getPaymentAccount(paymentAccounts);

        Organisation organisation = sut.findPaymentAccountsByEmail("some-email");
        assertThat(organisation).isNotNull();

        verify(organisationMock, times(1)).setPaymentAccounts(any());
        verify(organisationMock, times(1)).setUsers(any());
        verify(professionalUserRepositoryMock, times(1)).findByEmailAddress("some-email");
    }

    @Test
    public void testReturnEmptyOrganisationWhenUnKnownEmail() {
        Organisation  response = sut.findPaymentAccountsByEmail("some-email@gmail.com");
        assertThat(response).isNull();
        verify(professionalUserRepositoryMock, times(1)).findByEmailAddress(anyString());
    }

    @Test
    public void testEditPaymentAccountsByOrganisationIdentifier() {

        Organisation organisationMock = mock(Organisation.class);
        final List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(new PaymentAccount());
        ProfessionalUser professionalUserMock = mock(ProfessionalUser.class);

        when(professionalUserMock.getOrganisation()).thenReturn(organisationMock);
        when(organisationMock.getStatus()).thenReturn(OrganisationStatus.ACTIVE);
        when(organisationMock.getPaymentAccounts()).thenReturn(paymentAccounts);
        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisation);
        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.getTransaction()).thenReturn(entityTransactionMock);
        when(entityManagerMock.find(ArgumentMatchers.eq(UserAccountMap.class), any())).thenReturn(userAccountMapMock);
        when(entityManagerMock.find(ArgumentMatchers.eq(PaymentAccount.class), any())).thenReturn(paymentAccountMock);
        RefDataUtil.getPaymentAccount(paymentAccounts);

        PbaResponse response = sut.editPaymentAccountsByOrganisation(organisation, pbaEditRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo("200 OK");

        verify(paymentAccountRepositoryMock, times(1)).save(any(PaymentAccount.class));
        verify(userAccountMapServiceMock, times(1))
                .persistedUserAccountMap(any(ProfessionalUser.class), anyList());
        verify(entityManagerFactoryMock, times(1)).createEntityManager();
        verify(entityManagerMock, times(1)).getTransaction();
        verify(entityManagerMock, times(2)).find(any(), any());
        verify(entityManagerMock, times(2)).remove(any());
    }

    @Test
    public void testNotDeleteUserAccountMapsAndPaymentAccountsAreNull() {
        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisation);
        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.getTransaction()).thenReturn(entityTransactionMock);
        when(entityManagerMock.find(any(), any())).thenReturn(null);
        sut.deleteUserAccountMapsAndPaymentAccounts(entityManagerMock, organisation);

        assertThat(organisation.getPaymentAccounts()).isEmpty();
        verify(entityManagerMock, times(2)).find(any(), any());
        verify(entityManagerMock, times(0)).remove(any());
    }

    @Test
    public void testDeleteUserAccountMapsAndPaymentAccounts() {
        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisation);
        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.getTransaction()).thenReturn(entityTransactionMock);
        when(entityManagerMock.find(ArgumentMatchers.eq(UserAccountMap.class), any())).thenReturn(userAccountMapMock);
        when(entityManagerMock.find(ArgumentMatchers.eq(PaymentAccount.class), any())).thenReturn(paymentAccountMock);
        sut.deleteUserAccountMapsAndPaymentAccounts(entityManagerMock, organisation);

        assertThat(organisation.getPaymentAccounts()).isEmpty();
        verify(entityManagerMock, times(2)).find(any(), any());
        verify(entityManagerMock, times(2)).remove(any());
    }

    @Test
    public void testDeleteOnlyPaymentAccounts() {
        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisation);
        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.getTransaction()).thenReturn(entityTransactionMock);
        when(entityManagerMock.find(ArgumentMatchers.eq(UserAccountMap.class), any())).thenReturn(null);
        when(entityManagerMock.find(ArgumentMatchers.eq(PaymentAccount.class), any())).thenReturn(paymentAccountMock);
        sut.deleteUserAccountMapsAndPaymentAccounts(entityManagerMock, organisation);

        assertThat(organisation.getPaymentAccounts()).isEmpty();
        verify(entityManagerMock, times(2)).find(any(), any());
        verify(entityManagerMock, times(1)).remove(any(PaymentAccount.class));
    }


    @Test
    public void testAddPaymentAccountsToOrganisationTest() {
        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisation);
        sut.addPaymentAccountsToOrganisation(pbaEditRequest, organisation);
        verify(paymentAccountRepositoryMock, times(1)).save(any(PaymentAccount.class));
    }

    @Test
    public void testAddUserAndPaymentAccountsToUserAccountMapTest() {
        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisation);
        sut.addUserAndPaymentAccountsToUserAccountMap(organisation);
        assertThat(sut.addUserAndPaymentAccountsToUserAccountMap(organisation)).isNotNull();
        verify(userAccountMapServiceMock, times(2))
                .persistedUserAccountMap(any(ProfessionalUser.class), anyList());
    }

    @Test
    public void testGenerateListOfAccountsToDelete() {
        ProfessionalUser prefU = new ProfessionalUser("Con", "Hal",
                "email@gmail.com", organisation);
        List<UserAccountMapId> listUserMap = new ArrayList<>();
        assertThat(sut.generateListOfAccountsToDelete(prefU, paymentAccounts)).isNotNull();
        listUserMap = sut.generateListOfAccountsToDelete(prefU, paymentAccounts);
        assertThat(listUserMap.get(0).getProfessionalUser().getFirstName()).isEqualTo("Con");
    }
}