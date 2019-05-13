package uk.gov.hmcts.reform.professionalapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;

import uk.gov.hmcts.reform.professionalapi.persistence.*;

import uk.gov.hmcts.reform.professionalapi.util.ProfessionalReferenceDataClient;
import uk.gov.hmcts.reform.professionalapi.util.Service2ServiceEnabledIntegrationTest;

public class FindPaymentAccountsByEmailTest extends Service2ServiceEnabledIntegrationTest {

    @Autowired
    private OrganisationRepository organisationRepository;
    @Autowired
    private ProfessionalUserRepository professionalUserRepository;
    @Autowired
    private ContactInformationRepository contactInformationRepository;
    @Autowired
    private DxAddressRepository dxAddressRepository;
    @Autowired
    PaymentAccountRepository paymentAccountRepository;

    private ProfessionalReferenceDataClient professionalReferenceDataClient;

    @Before
    public void setUp() {
        professionalReferenceDataClient = new ProfessionalReferenceDataClient(port);
        dxAddressRepository.deleteAll();
        contactInformationRepository.deleteAll();
        professionalUserRepository.deleteAll();
        paymentAccountRepository.deleteAll();
        organisationRepository.deleteAll();
    }

    @Test
    public void get_request_returns_correct_payment_accounts() {
        Organisation newOrganisation = new Organisation("some-org-", "pending", "sra-id", "comp-num", false, "company-url");
        Organisation theOrganisation = organisationRepository.save(newOrganisation);

        PaymentAccount thePaymentAcc = new PaymentAccount("pbaNum");
        thePaymentAcc.setOrganisation(theOrganisation);
        paymentAccountRepository.save(thePaymentAcc);
        theOrganisation.addPaymentAccount(thePaymentAcc);

        ProfessionalUser theSuperUser = new ProfessionalUser("some-fname", "some-lname", "some@email.com", "status", theOrganisation);
        professionalUserRepository.save((theSuperUser));
        theOrganisation.addProfessionalUser(theSuperUser);

        ContactInformation theContactInfo = new ContactInformation("addressLine-1", "addressLine-2", "addressLine-3", "townCity", "county", "country", "postCode", theOrganisation);
        contactInformationRepository.save(theContactInfo);
        theOrganisation.addContactInformation(theContactInfo);

        organisationRepository.save(theOrganisation);

        Map<String, Object> response =
                professionalReferenceDataClient.findPaymentAccountsByEmail("some@email.com");

        System.out.println("RESPONSE:: " + response);

        String responseString = response.toString();

        assertEquals("{organisationEntityResponse={organisationIdentifier=" + theOrganisation.getOrganisationIdentifier().toString() + ", name=some-org-, status=pending, sraId=sra-id, sraRegulated=false, companyNumber=comp-num, companyUrl=company-url, superUser=[{firstName=some-fname, lastName=some-lname, email=some@email.com}], pbaAccounts=[{pbaNumber=pbaNum}]}, http_status=200 OK}", responseString);

    }

    @Test
    public void returns_404_when_email_not_found() {
        Map<String, Object> response =
                professionalReferenceDataClient.findPaymentAccountsByEmail("wrong@email.com");

        assertThat(response.get("http_status")).isEqualTo("404");
    }

}
