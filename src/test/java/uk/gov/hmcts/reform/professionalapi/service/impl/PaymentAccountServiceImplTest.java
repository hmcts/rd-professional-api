package uk.gov.hmcts.reform.professionalapi.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.professionalapi.configuration.ApplicationConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaUpdateRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.controller.response.PbaUpdateStatusResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UpdatePbaStatusResponse;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_PARTIAL_SUCCESS_UPDATE;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_PBA_INVALID_FORMAT;
import static uk.gov.hmcts.reform.professionalapi.controller.constants.ProfessionalApiConstants.ERROR_MSG_STATUS_INVALID;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.ACCEPTED;
import static uk.gov.hmcts.reform.professionalapi.domain.PbaStatus.PENDING;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class PaymentAccountServiceImplTest {

    private final ApplicationConfiguration applicationConfigurationMock = mock(ApplicationConfiguration.class);
    private final ProfessionalUserRepository professionalUserRepositoryMock = mock(ProfessionalUserRepository.class);
    private final UserProfileFeignClient userProfileFeignClientMock = mock(UserProfileFeignClient.class);
    private final PaymentAccountRepository paymentAccountRepositoryMock = mock(PaymentAccountRepository.class);
    private final OrganisationRepository organisationRepositoryMock = mock(OrganisationRepository.class);
    private final UserAccountMapService userAccountMapServiceMock = mock(UserAccountMapService.class);
    private final UserAccountMap userAccountMapMock = mock(UserAccountMap.class);
    private final PaymentAccountValidator paymentAccountValidatorMock = mock(PaymentAccountValidator.class);
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
    private final List<SuperUser> superUsers = new ArrayList<>();
    private final List<PaymentAccount> paymentAccounts = new ArrayList<>();
    private final Set<String> pbas = new HashSet<>();
    private final PbaRequest pbaEditRequest = new PbaRequest();
    private final PbaRequest pbaDeleteRequest = new PbaRequest();

    @BeforeEach
    void setUp() {
        organisationService = new OrganisationServiceImpl();
        organisationService.setPaymentAccountValidator(paymentAccountValidatorMock);
        organisationService.setPaymentAccountRepository(paymentAccountRepositoryMock);

        sut = new PaymentAccountServiceImpl(applicationConfigurationMock, userProfileFeignClientMock,
                entityManagerFactoryMock, professionalUserRepositoryMock, organisationService,
                userAccountMapServiceMock, paymentAccountRepositoryMock);

        superUsers.add(superUser);
        paymentAccounts.add(paymentAccount);
        pbas.add("PBA0000001");
        pbaEditRequest.setPaymentAccounts(pbas);
        pbaDeleteRequest.setPaymentAccounts(pbas);
        userAccountMaps.add(userAccountMapMock);

        paymentAccount.setId(UUID.randomUUID());

        organisation.setPaymentAccounts(paymentAccounts);
        organisation.setOrganisationIdentifier("AK57L4T");
        organisation.setUsers(superUsers);
    }

    @Test
    void testRetrievePaymentAccountsByPbaEmail() {
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
    void testReturnEmptyOrganisationWhenUnKnownEmail() {
        Organisation  response = sut.findPaymentAccountsByEmail("some-email@gmail.com");
        assertThat(response).isNull();
        verify(professionalUserRepositoryMock, times(1)).findByEmailAddress(anyString());
    }

    @Test
    void testEditPaymentAccountsByOrganisationIdentifier() {

        final List<PaymentAccount> paymentAccounts = new ArrayList<>();
        paymentAccounts.add(new PaymentAccount());

        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisation);
        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.getTransaction()).thenReturn(entityTransactionMock);

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
        verify(entityManagerMock, times(1)).close();
        verify(entityTransactionMock, times(1)).begin();
        verify(entityTransactionMock, times(1)).commit();
    }

    @Test
    void testDeleteUserAccountMapsAndPaymentAccounts() {
        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisation);
        when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
        when(entityManagerMock.getTransaction()).thenReturn(entityTransactionMock);

        sut.deleteUserAccountMapsAndPaymentAccounts(entityManagerMock, organisation);

        assertThat(organisation.getPaymentAccounts()).isEmpty();
        verify(entityManagerMock, times(2)).find(any(), any());
        verify(entityManagerMock, times(2)).remove(any());
    }

    @Test
    void testAddPaymentAccountsToOrganisationTest() {
        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisation);
        sut.addPaymentAccountsToOrganisation(pbaEditRequest, organisation);
        verify(paymentAccountRepositoryMock, times(1)).save(any(PaymentAccount.class));
    }

    @Test
    void testDeletePaymentAccountsFromOrganisationTest() {
        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisation);
        sut.deletePaymentsOfOrganisation(pbaDeleteRequest, organisation);
        verify(paymentAccountRepositoryMock, times(1)).deleteByPbaNumberUpperCase(anySet());
    }

    @Test
    void testAddUserAndPaymentAccountsToUserAccountMapTest() {
        when(organisationRepositoryMock.findByOrganisationIdentifier(any(String.class))).thenReturn(organisation);
        sut.addUserAndPaymentAccountsToUserAccountMap(organisation);
        assertThat(sut.addUserAndPaymentAccountsToUserAccountMap(organisation)).isNotNull();
        verify(userAccountMapServiceMock, times(2))
                .persistedUserAccountMap(any(ProfessionalUser.class), anyList());
    }

    @Test
    void testGenerateListOfAccountsToDelete() {
        ProfessionalUser prefU = new ProfessionalUser("Con", "Hal",
                "email@gmail.com", organisation);
        List<UserAccountMapId> listUserMap;
        assertThat(sut.generateListOfAccountsToDelete(prefU, paymentAccounts)).isNotNull();
        listUserMap = sut.generateListOfAccountsToDelete(prefU, paymentAccounts);
        assertThat(listUserMap.get(0).getProfessionalUser().getFirstName()).isEqualTo("Con");
        assertThat(listUserMap.get(0).getPaymentAccount().getPbaNumber()).isEqualTo("PBA1234567");
    }

    @Test
    void testUpdatePaymentAccountsForAnOrganisation_200_success_scenario() {
        String pbaNumber = "PBA1234567";

        PaymentAccount paymentAccount = mock(PaymentAccount.class);
        when(paymentAccount.getPbaNumber()).thenReturn(pbaNumber);
        when(paymentAccount.getPbaStatus()).thenReturn(PENDING);
        when(paymentAccount.getOrganisation()).thenReturn(organisation);

        List<PbaUpdateRequest> pbaRequestList = new ArrayList<>();

        pbaRequestList.add(new PbaUpdateRequest(pbaNumber, ACCEPTED.name(), ""));

        when(paymentAccountRepositoryMock.findByPbaNumberIn(Set.of(pbaNumber))).thenReturn(asList(paymentAccount));

        UpdatePbaStatusResponse response = sut
                .updatePaymentAccountsStatusForAnOrganisation(pbaRequestList, organisation.getOrganisationIdentifier());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getPartialSuccessMessage()).isNull();
        assertThat(response.getPbaUpdateStatusResponses()).isNull();
    }

    @Test
    void testUpdatePaymentAccountsForAnOrganisation_200_partial_success_scenario() {
        String pbaNumber = "PBA1234567";

        PaymentAccount paymentAccount = mock(PaymentAccount.class);
        when(paymentAccount.getPbaNumber()).thenReturn(pbaNumber);
        when(paymentAccount.getPbaStatus()).thenReturn(PENDING);
        when(paymentAccount.getOrganisation()).thenReturn(organisation);

        List<PbaUpdateRequest> pbaRequestList = new ArrayList<>();

        pbaRequestList.add(new PbaUpdateRequest(pbaNumber, ACCEPTED.name(), ""));
        pbaRequestList.add(new PbaUpdateRequest("PBA123", ACCEPTED.name(), ""));

        when(paymentAccountRepositoryMock.findByPbaNumberIn(Set.of(pbaNumber))).thenReturn(asList(paymentAccount));

        UpdatePbaStatusResponse response = sut
                .updatePaymentAccountsStatusForAnOrganisation(pbaRequestList, organisation.getOrganisationIdentifier());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getPartialSuccessMessage()).contains(ERROR_MSG_PARTIAL_SUCCESS_UPDATE);
        assertThat(response.getPbaUpdateStatusResponses().get(0).getPbaNumber()).contains("PBA123");
        assertThat(response.getPbaUpdateStatusResponses().get(0).getErrorMessage())
                .contains(ERROR_MSG_PBA_INVALID_FORMAT);
        assertThat(pbaRequestList.get(0).getPbaNumber()).isEqualTo("PBA1234567");
        assertThat(pbaRequestList.get(1).getPbaNumber()).isEqualTo("PBA123");
    }

    @Test
    void testUpdatePaymentAccountsForAnOrganisation_422_failure_scenario() {
        String orgId = UUID.randomUUID().toString();
        List<PbaUpdateRequest> pbaRequestList = new ArrayList<>();

        pbaRequestList.add(new PbaUpdateRequest("PBA1234567", "REJUCTED", ""));

        UpdatePbaStatusResponse response = sut.updatePaymentAccountsStatusForAnOrganisation(pbaRequestList, orgId);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(422);
        assertThat(response.getPartialSuccessMessage()).isNull();
        assertThat(response.getPbaUpdateStatusResponses().get(0).getErrorMessage()).contains(ERROR_MSG_STATUS_INVALID);
    }

    @Test
    void testUpdatePBAsInDb() {
        PaymentAccount paymentAccount = new PaymentAccount("PBA1234567");
        PaymentAccount paymentAccount1 = new PaymentAccount("PBA7654321");

        List<PaymentAccount> pbasToDelete = new ArrayList<>();
        pbasToDelete.add(paymentAccount);

        List<PaymentAccount> pbasToSave = new ArrayList<>();
        pbasToSave.add(paymentAccount1);

        sut.updatePBAsInDb(pbasToSave, pbasToDelete);

        verify(paymentAccountRepositoryMock, times(1)).saveAll(pbasToSave);
        verify(paymentAccountRepositoryMock, times(1)).deleteAll(pbasToDelete);
    }

    @Test
    void testGetStatusMessageFromRequest_StatusMessageNull() {
        PbaUpdateRequest pbaUpdateRequest = new PbaUpdateRequest("PBA123", "ACCEPTED", null);

        String message = sut.getStatusMessageFromRequest(pbaUpdateRequest);

        assertThat(message).isEmpty();
    }

    @Test
    void testGetStatusMessageFromRequest_StatusMessageNotNull() {
        PbaUpdateRequest pbaUpdateRequest = new PbaUpdateRequest("PBA123", "ACCEPTED", "MESSAGE");

        String message = sut.getStatusMessageFromRequest(pbaUpdateRequest);

        assertThat(message).isEqualTo("MESSAGE");
    }

    @Test
    void testAcceptOrRejectPbas() {
        PaymentAccount paymentAccount = mock(PaymentAccount.class);
        when(paymentAccount.getPbaNumber()).thenReturn("PBA1234567");
        when(paymentAccount.getPbaStatus()).thenReturn(PENDING);
        PaymentAccount paymentAccount1 = new PaymentAccount("PBA7654321");
        paymentAccount1.setPbaStatus(ACCEPTED);
        List<PaymentAccount> pbasFromDb = new ArrayList<>();
        pbasFromDb.add(paymentAccount);
        pbasFromDb.add(paymentAccount1);

        List<PbaUpdateRequest> pbaRequestList = new ArrayList<>();
        PbaUpdateRequest pbaUpdateRequest = new PbaUpdateRequest("PBA1234567", "ACCEPTED", "ACCEPTED STATUS");
        pbaRequestList.add(pbaUpdateRequest);
        PbaUpdateRequest pbaUpdateRequest1 = new PbaUpdateRequest("PBA7654321", "REJECTED", "");
        pbaRequestList.add(pbaUpdateRequest1);

        List<PbaUpdateStatusResponse> response = sut.acceptOrRejectPbas(pbasFromDb, pbaRequestList, new ArrayList<>());

        assertThat(response).isNotNull();
        assertThat(pbaRequestList.get(0).getPbaNumber()).isEqualTo("PBA1234567");
        assertThat(pbaRequestList.get(1).getPbaNumber()).isEqualTo("PBA7654321");

        verify(paymentAccount, times(1)).setPbaStatus(ACCEPTED);
        verify(paymentAccount, times(1)).setStatusMessage("ACCEPTED STATUS");
        verify(paymentAccountRepositoryMock, times(1)).deleteAll(any());
    }
}