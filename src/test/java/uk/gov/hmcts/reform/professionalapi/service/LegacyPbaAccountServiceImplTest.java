package uk.gov.hmcts.reform.professionalapi.service;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccountMap;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.LegacyPbaAccountServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserAttributeServiceImpl;


public class LegacyPbaAccountServiceImplTest {


    private final Organisation organisationMock = mock(Organisation.class);
    private final PaymentAccount paymentAccountMock = mock(PaymentAccount.class);
    private final UserAccountMap userAccountMapMock = mock(UserAccountMap.class);
    private List<ProfessionalUser> usersNonEmptyList = new ArrayList<ProfessionalUser>();
    private List<PaymentAccount> paymentAccounts = new ArrayList<PaymentAccount>();
    private List<UserAccountMap> userAccountMaps = new ArrayList<>();


    List<String> paymentAccountPbaNumbers = new ArrayList<>();
    private final ProfessionalUserRepository professionalUserRepository = mock(ProfessionalUserRepository.class);
    private final LegacyPbaAccountService legacyPbaAccountServiceImpl = mock(LegacyPbaAccountServiceImpl.class);

    private final OrganisationRepository organisationRepository = mock(OrganisationRepository.class);

    private final ProfessionalUser professionalUser = mock(ProfessionalUser.class);
    private final Organisation organisation = mock(Organisation.class);

    private ProfessionalUserServiceImpl professionalUserServiceImpl;
    UserAttributeRepository userAttributeRepository;
    PrdEnumRepository prdEnumRepository;

    UserAttributeServiceImpl userAttributeService;

    @Before
    public void setUp() {

        professionalUserServiceImpl = new ProfessionalUserServiceImpl(
                organisationRepository,
                professionalUserRepository,
                userAttributeRepository,
                prdEnumRepository,
                userAttributeService);


        List<PaymentAccount>  paymentAccountsFromEntity = new ArrayList<>();

        when(professionalUserServiceImpl.findProfessionalUserByEmailAddress(any(String.class)))
                .thenReturn(professionalUser);

        when(professionalUser.getOrganisation()).thenReturn(organisation);

        when(organisation.getPaymentAccounts()).thenReturn(paymentAccounts);

        when(professionalUser.getUserAccountMap()).thenReturn(userAccountMaps);

        when(legacyPbaAccountServiceImpl.findLegacyPbaAccountByUserEmail(any(ProfessionalUser.class)))
                .thenReturn(paymentAccountPbaNumbers);

    }

    @Test
    public void findLegacyPbaAccountByUserEmailTest() {

        ProfessionalUser professionalUser = professionalUserServiceImpl.findProfessionalUserByEmailAddress("some-email");

        assertThat(professionalUser).isNotNull();

        paymentAccountPbaNumbers = legacyPbaAccountServiceImpl.findLegacyPbaAccountByUserEmail(professionalUser);

        assertThat(paymentAccountPbaNumbers).isNotNull();
        verify(
                professionalUserRepository,
                times(1)).findByEmailAddress(any(String.class));

        verify(
                legacyPbaAccountServiceImpl,
                times(1)).findLegacyPbaAccountByUserEmail(any(ProfessionalUser.class));

    }

    @Test
    public void findLegacyPbaAccountsByUserEmailTest() {

        Organisation theOrganisation = new Organisation("some-org-", OrganisationStatus.PENDING, "sra-id", "company-number", false, "company-url");

        ProfessionalUser theSuperUser = new ProfessionalUser("some-fname", "some-lname", "some-email", ProfessionalUserStatus.PENDING, theOrganisation);
        theOrganisation.addProfessionalUser(theSuperUser);

        ContactInformation theContactInfo = new ContactInformation("addressLine-1", "addressLine-2", "addressLine-3", "townCity", "county", "country", "postCode", theOrganisation);
        theOrganisation.addContactInformation(theContactInfo);

        PaymentAccount thePaymentAcc = new PaymentAccount("pbaNumber-1");
        theOrganisation.addPaymentAccount(thePaymentAcc);

        organisationRepository.save(theOrganisation);

        ProfessionalUser professionalUser = professionalUserServiceImpl.findProfessionalUserByEmailAddress("some-email");

        List<String> paymentAccountPbaNumbers = legacyPbaAccountServiceImpl.findLegacyPbaAccountByUserEmail(professionalUser);

        assertThat(paymentAccountPbaNumbers).isNotNull();

    }
}