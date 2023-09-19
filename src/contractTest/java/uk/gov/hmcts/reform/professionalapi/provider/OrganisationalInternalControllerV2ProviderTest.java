package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import uk.gov.hmcts.reform.professionalapi.controller.internal.OrganisationInternalControllerV2;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.OrgAttribute;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaResponse;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Provider("referenceData_organisationalInternalV2")
@Import(OrganisationalInternalControllerProviderTestConfiguration.class)
public class OrganisationalInternalControllerV2ProviderTest extends MockMvcProviderTest {

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    OrganisationInternalControllerV2 organisationInternalControllerV2;


    @Autowired
    PaymentAccountService paymentAccountService;

    @Autowired
    MappingJackson2HttpMessageConverter httpMessageConverter;

    public static final String ORG_NAME = "Org-Name";
    public static final String SRA_ID = "sra-id";
    public static final String COMPANY_NUMBER = "companyN";
    public static final String COMPANY_URL = "www.org.com";

    @Override
    void setController() {
        testTarget.setControllers(organisationInternalControllerV2);
        testTarget.setMessageConverters(httpMessageConverter);
    }

    //retrieveOrganisations
    @State("Organisation V2 exists for given Id")
    public void setUpOrganisationForGivenId() {

        Organisation organisation = getOrganisation();
        when(organisationRepository.findByOrganisationIdentifier(anyString())).thenReturn(organisation);

    }

    //retrieveOrganisationsWithPagination
    @State("An organisation V2 exists with pagination")
    public void setUpOrganisationWithPagination() {

        Organisation organisation = getOrganisation();
        Page<Organisation> orgPage = mock(Page.class);

        when(organisationRepository.findByStatusIn(List.of(OrganisationStatus.ACTIVE, OrganisationStatus.PENDING),
            any(Pageable.class))).thenReturn(orgPage);
        when(orgPage.getContent()).thenReturn(List.of(organisation));
    }


    //retrieveOrganisationsWithStatusAndPagination
    @State("An active organisation V2 exists for given status and with pagination")
    public void setUpOrganisationWithStatusAndPagination() {

        Organisation organisation = new Organisation(ORG_NAME, OrganisationStatus.ACTIVE, SRA_ID,
            COMPANY_NUMBER, false, COMPANY_URL);
        addSuperUser(organisation);
        when(organisationRepository.findByStatusIn(List.of(OrganisationStatus.ACTIVE), any(Pageable.class)))
            .thenReturn(mock(Page.class));
        when(organisationRepository.findByStatusIn(List.of(OrganisationStatus.ACTIVE), any(Pageable.class))
                .getContent()).thenReturn(List.of(organisation));
    }


    @State("An Organisation V2 exists for update")
    public void setUpOrganisationForUpdate() {

        Organisation organisation = new Organisation(ORG_NAME, OrganisationStatus.PENDING, SRA_ID,
                COMPANY_NUMBER, false, COMPANY_URL);
        addSuperUser(organisation);

        Organisation updatedOrganisation = new Organisation(ORG_NAME, OrganisationStatus.PENDING, SRA_ID,
                COMPANY_NUMBER, false, COMPANY_URL);
        addSuperUser(organisation);

        when(organisationRepository.findByOrganisationIdentifier(anyString())).thenReturn(organisation);
        when(organisationRepository.save(any())).thenReturn(updatedOrganisation);

    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    @State("An Organisation V2 with PBA accounts exists")
    public void setUpOrganisationForPBAsUpdate() {

        Organisation organisation = new Organisation(ORG_NAME, OrganisationStatus.PENDING, SRA_ID,
                COMPANY_NUMBER, false, COMPANY_URL);
        addSuperUser(organisation);

        when(organisationRepository.findByOrganisationIdentifier(anyString())).thenReturn(organisation);

        when(paymentAccountService.editPaymentAccountsByOrganisation(any(Organisation.class),
            any(PbaRequest.class)))
            .thenReturn(new PbaResponse("200", "Success"));
    }


    private void addSuperUser(Organisation organisation) {
        SuperUser superUser = new SuperUser("some-fname", "some-lname",
                "some-email-address", organisation);
        superUser.setUserIdentifier(UUID.randomUUID().toString());
        List<SuperUser> users = new ArrayList<>();
        users.add(superUser);
        organisation.setStatus(OrganisationStatus.ACTIVE);
        organisation.setUsers(users);
    }

    private Organisation getOrganisation() {
        Organisation organisation = new Organisation(ORG_NAME, OrganisationStatus.PENDING, SRA_ID,
                COMPANY_NUMBER, false, COMPANY_URL);
        organisation.setSraRegulated(true);
        organisation.setOrgType("123");
        OrgAttribute orgAttribute = new OrgAttribute();
        orgAttribute.setKey("123");
        orgAttribute.setValue("ACCA");
        organisation.setOrgAttributes(List.of(orgAttribute));
        organisation.setOrganisationIdentifier("someOrganisationIdentifier");
        ContactInformation contactInformation = new ContactInformation();
        contactInformation.setUprn("uprn");
        contactInformation.setAddressLine1("addressLine1");
        contactInformation.setAddressLine2("addressLine2");
        contactInformation.setCountry("country");
        contactInformation.setPostCode("HA5 1BJ");
        contactInformation.setCreated(LocalDateTime.now());
        contactInformation.setId(UUID.randomUUID());
        organisation.setContactInformations(List.of(contactInformation));
        return organisation;
    }


    @State("Organisations V2 with payment accounts exist for given Pba Email")
    public void setUpOrganisationWithStatusForGivenPbaEmail() {
        Organisation organisation = getOrganisationWithPbaEmail();
        when(organisationRepository.findByPbaStatus(any())).thenReturn(List.of(organisation));
    }

    private Organisation getOrganisationWithPbaEmail() {
        PaymentAccount paymentAccount = new PaymentAccount();
        paymentAccount.setPbaNumber("PBA12345");
        paymentAccount.setStatusMessage("Approved");
        paymentAccount.setPbaStatus(PbaStatus.ACCEPTED);
        paymentAccount.setCreated(LocalDateTime.now());
        paymentAccount.setLastUpdated(LocalDateTime.now());
        Organisation organisation = new Organisation(ORG_NAME, OrganisationStatus.ACTIVE, SRA_ID,
                COMPANY_NUMBER, false, COMPANY_URL);
        organisation.setSraRegulated(true);
        organisation.setOrganisationIdentifier("org1");
        organisation.setPaymentAccounts(Collections.singletonList(paymentAccount));
        SuperUser superUser = new SuperUser();
        superUser.setFirstName("fName");
        superUser.setLastName("lName");
        superUser.setEmailAddress("example.email@test.com");
        organisation.setUsers(Collections.singletonList(superUser));
        OrgAttribute orgAttribute = new OrgAttribute();
        orgAttribute.setKey("123");
        orgAttribute.setValue("ACCA");
        organisation.setOrgAttributes(Collections.singletonList(orgAttribute));
        organisation.setOrgType("some");
        return organisation;
    }
}