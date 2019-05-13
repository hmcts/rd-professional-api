package uk.gov.hmcts.reform.professionalapi.service;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;

import org.powermock.api.mockito.PowerMockito;
import uk.gov.hmcts.reform.professionalapi.domain.*;
import uk.gov.hmcts.reform.professionalapi.persistence.*;

public class PaymentAccountServiceTest {

    private final ProfessionalUserRepository professionalUserRepository = mock(ProfessionalUserRepository.class);
    private final OrganisationRepository organisationRepository = mock(OrganisationRepository.class);

    private final ProfessionalUser professionalUser = mock(ProfessionalUser.class);
    private final Organisation organisation = mock(Organisation.class);

    private PaymentAccountService paymentAccountService;

    @Before
    public void setUp() {
        paymentAccountService = new PaymentAccountService(
                organisationRepository,
                professionalUserRepository);

        PowerMockito.when(organisationRepository.save(any(Organisation.class)))
                .thenReturn(organisation);

        PowerMockito.when(professionalUserRepository.findByEmailAddress(any(String.class)))
                .thenReturn(professionalUser);

        PowerMockito.when(organisationRepository.findByUsers(any(ProfessionalUser.class)))
                .thenReturn(organisation);
    }

    @Test
    public void retrievePaymentAccountsByEmail() {
        Organisation theOrganisation = new Organisation("some-org-", "pending", "sra-id", "company-number", false, "company-url");
        PowerMockito.when(paymentAccountService.findPaymentAccountsByEmail("some-email")).thenReturn(theOrganisation);

        ProfessionalUser theSuperUser = new ProfessionalUser("some-fname", "some-lname", "some-email", "status", theOrganisation);
        theOrganisation.addProfessionalUser(theSuperUser);

        ContactInformation theContactInfo = new ContactInformation("addressLine-1", "addressLine-2", "addressLine-3", "townCity", "county", "country", "postCode", theOrganisation);
        theOrganisation.addContactInformation(theContactInfo);

        PaymentAccount thePaymentAcc = new PaymentAccount("pbaNumber-1");
        theOrganisation.addPaymentAccount(thePaymentAcc);

        organisationRepository.save(theOrganisation);

        Organisation anOrganisation = paymentAccountService.findPaymentAccountsByEmail("some-email");
        assertThat(anOrganisation).isNotNull();

        assertEquals(anOrganisation.getName(), theOrganisation.getName());
        assertEquals(anOrganisation.getSraId(), theOrganisation.getSraId());
        assertEquals(anOrganisation.getCompanyNumber(), theOrganisation.getCompanyNumber());
        assertEquals(anOrganisation.getSraRegulated(), theOrganisation.getSraRegulated());
        assertEquals(anOrganisation.getCompanyUrl(), theOrganisation.getCompanyUrl());
        assertEquals(anOrganisation.getUsers(), (theOrganisation.getUsers()));
        assertEquals(anOrganisation.getPaymentAccounts(), theOrganisation.getPaymentAccounts());
    }

    public void retrieveUserByEmailNotFound() {
        PowerMockito.when(professionalUserRepository.findByEmailAddress(any(String.class)))
                .thenReturn(null);

        paymentAccountService.findPaymentAccountsByEmail("some-email");

        assertEquals("", organisation.getOrganisationIdentifier().toString());
    }
}