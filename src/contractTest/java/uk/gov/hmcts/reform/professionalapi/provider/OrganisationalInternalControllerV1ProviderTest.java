package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.professionalapi.controller.internal.OrganisationInternalController;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.Mockito.when;

@Provider("referenceData_organisationalDetailsInternal")
@Import(OrganisationalInternalControllerV1ProviderTestConfiguration.class)
public class OrganisationalInternalControllerV1ProviderTest extends MockMvcProviderTest {

    @Autowired
    OrganisationInternalController organisationInternalController;

    @Autowired
    OrganisationServiceImpl organisationServiceImpl;

    @Autowired
    ProfessionalUserRepository professionalUserRepositoryMock;

    private Organisation organisation;

    @Override
    void setController() {
        testTarget.setControllers(organisationInternalController);
    }

    @State("organisation exists for given Id")
    public void toRetrieveOrganisationByUserId()  {

        when(professionalUserRepositoryMock.findByUserIdentifier("someUid")).thenReturn(
                setUpProfessionalUser());

    }

    private ProfessionalUser setUpProfessionalUser() {
        Organisation organisation = new Organisation();
        organisation.setName("Possession Claims Solicitor Org");
        organisation.setDateApproved(LocalDateTime.parse("2025-09-11T13:56:40.778072"));
        //organisation.setDateReceived("2025-09-11T13:46:54.42977");
        organisation.setLastUpdated(LocalDateTime.parse("2025-09-11T13:56:40.77853"));
        organisation.setOrganisationIdentifier("E71FH4Q");
        organisation.setSraRegulated(false);
        organisation.setStatus(OrganisationStatus.ACTIVE);

        PaymentAccount paymentAccount1 = new PaymentAccount();
        paymentAccount1.setPbaNumber("PBA0078010");

        PaymentAccount paymentAccount2 = new PaymentAccount();
        paymentAccount2.setPbaNumber("PBA0078011");
        organisation.setPaymentAccounts(Arrays.asList(paymentAccount1,paymentAccount2));

        SuperUser superUser = new SuperUser();
        superUser.setEmailAddress("pcs-solicitor-org-adm@mailinator.com");
        superUser.setFirstName("Solicitor");
        superUser.setLastName("Admin Org");
        organisation.addProfessionalUser(superUser);

        ContactInformation contactInformation = new ContactInformation();
        //contactInformation.setAddressId("98b33d54-2a0b-4da0-8b8c-5215b0fc114b");
        contactInformation.setAddressLine1("Ministry Of Justice");
        contactInformation.setAddressLine2("Seventh Floor 102 Petty France");
        contactInformation.setTownCity("London");
        contactInformation.setCountry("United Kingdom");
        contactInformation.setCreated(LocalDateTime.parse("2025-09-11T13:46:54.529947"));
        contactInformation.setPostCode("SW1H 9AJ");
        organisation.addContactInformation(contactInformation);

        ProfessionalUser pu = new ProfessionalUser();
        pu.setOrganisation(organisation);
        return pu;
    }
}
