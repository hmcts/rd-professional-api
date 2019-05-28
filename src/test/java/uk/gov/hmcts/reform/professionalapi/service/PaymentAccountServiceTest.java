package uk.gov.hmcts.reform.professionalapi.service;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Before;
import org.junit.Test;

import org.powermock.api.mockito.PowerMockito;
import org.springframework.dao.EmptyResultDataAccessException;

import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.PaymentAccountServiceImpl;



public class PaymentAccountServiceTest {

    private final ProfessionalUserRepository professionalUserRepository = mock(ProfessionalUserRepository.class);
    private final OrganisationRepository organisationRepository = mock(OrganisationRepository.class);

    private final ProfessionalUser professionalUser = mock(ProfessionalUser.class);
    private final Organisation organisation = mock(Organisation.class);

    private PaymentAccountServiceImpl paymentAccountService;

    @Before
    public void setUp() {
        paymentAccountService = new PaymentAccountServiceImpl(
                organisationRepository,
                professionalUserRepository);

        when(organisationRepository.save(any(Organisation.class)))
                .thenReturn(organisation);

        when(professionalUserRepository.findByEmailAddress(any(String.class)))
                .thenReturn(professionalUser);

        when(organisationRepository.findByUsers(any(ProfessionalUser.class)))
                .thenReturn(organisation);
    }

    @Test
    public void retrievePaymentAccountsByEmail() {
        Organisation theOrganisation = new Organisation("some-org-", OrganisationStatus.PENDING, "sra-id", "company-number", false, "company-url");

        ProfessionalUser theSuperUser = new ProfessionalUser("some-fname", "some-lname", "some-email", ProfessionalUserStatus.PENDING, theOrganisation);
        theOrganisation.addProfessionalUser(theSuperUser);

        ContactInformation theContactInfo = new ContactInformation("addressLine-1", "addressLine-2", "addressLine-3", "townCity", "county", "country", "postCode", theOrganisation);
        theOrganisation.addContactInformation(theContactInfo);

        PaymentAccount thePaymentAcc = new PaymentAccount("pbaNumber-1");
        theOrganisation.addPaymentAccount(thePaymentAcc);

        organisationRepository.save(theOrganisation);

        when(paymentAccountService.findPaymentAccountsByEmail("some-email")).thenReturn(theOrganisation);

        Organisation anOrganisation = paymentAccountService.findPaymentAccountsByEmail("some-email");
        assertThat(anOrganisation).isNotNull();

        assertEquals(anOrganisation.getName(), theOrganisation.getName());
        assertEquals(anOrganisation.getSraId(), theOrganisation.getSraId());
        assertEquals(anOrganisation.getStatus(), theOrganisation.getStatus());
        assertEquals(anOrganisation.getCompanyNumber(), theOrganisation.getCompanyNumber());
        assertEquals(anOrganisation.getSraRegulated(), theOrganisation.getSraRegulated());
        assertEquals(anOrganisation.getCompanyUrl(), theOrganisation.getCompanyUrl());
        assertEquals(anOrganisation.getUsers(), (theOrganisation.getUsers()));
        assertEquals(anOrganisation.getPaymentAccounts(), theOrganisation.getPaymentAccounts());
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void retrieveUserByEmailNotFound() {
        PowerMockito.when(professionalUserRepository.findByEmailAddress(any(String.class)))
                .thenReturn(null);

        paymentAccountService.findPaymentAccountsByEmail("some-email");

    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void throwsExceptionWhenUserIsNull() {

        paymentAccountService.findPaymentAccountsByEmail(null);

    }


    @Test(expected = EmptyResultDataAccessException.class)
    public void retrievePaymentAccountsWithInvalidEmail() {
        when(paymentAccountService.findPaymentAccountsByEmail("some-email"))
                .thenReturn(null);

        paymentAccountService.findPaymentAccountsByEmail(null);
    }


}