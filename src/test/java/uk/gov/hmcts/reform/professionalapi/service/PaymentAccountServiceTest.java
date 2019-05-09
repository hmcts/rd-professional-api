package uk.gov.hmcts.reform.professionalapi.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.xml.ws.http.HTTPException;
import org.junit.Test;

import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;

public class PaymentAccountServiceTest {

    private final ProfessionalUserRepository professionalUserRepository = mock(ProfessionalUserRepository.class);
    private final OrganisationRepository organisationRepository = mock(OrganisationRepository.class);

    private final ProfessionalUser professionalUser = new ProfessionalUser("some-fname",
            "some-lname",
            "some-email",
            "PENDING",
            mock(Organisation.class));

    private final Organisation organisation = new Organisation("some-org-name",
            "status",
            "sra-id",
            "company-number",
            false,
            "ord-identifier");

    private final PaymentAccountService paymentAccountService = new PaymentAccountService(
            organisationRepository,
            professionalUserRepository);

    @Test
    public void retrievePaymentAccountsByEmail() {
        when(professionalUserRepository.findByEmailAddress(any(String.class)))
                .thenReturn(professionalUser);
        when(organisationRepository.findByUsers(any(ProfessionalUser.class)))
                .thenReturn(organisation);

        Organisation anOrganisation = paymentAccountService.findPaymentAccountsByEmail("some-email");
        assertEquals(organisation.getName(), anOrganisation.getName());
        assertEquals(organisation.getStatus(), anOrganisation.getStatus());
        assertEquals(organisation.getSraId(), anOrganisation.getSraId());
        assertEquals(organisation.getCompanyNumber(), anOrganisation.getCompanyNumber());
        assertEquals(organisation.getSraRegulated(), anOrganisation.getSraRegulated());
        assertEquals(organisation.getCompanyUrl(), anOrganisation.getCompanyUrl());
    }

    @Test(expected = HTTPException.class)
    public void retrieveUserByEmailNotFound() {
        when(professionalUserRepository.findByEmailAddress(any(String.class)))
                .thenReturn(null);

        paymentAccountService.findPaymentAccountsByEmail("some-email");
    }

}