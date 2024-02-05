package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.internal.OrganisationInternalControllerV2;
import uk.gov.hmcts.reform.professionalapi.controller.request.OrganisationOtherOrgsCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationsDetailResponseV2;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.OrgAttribute;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Provider("referenceData_organisationalInternalV2")
@Import(OrganisationalInternalControllerV2ProviderTestConfiguration.class)
public class OrganisationalInternalControllerV2ProviderTest extends MockMvcProviderTest {


    private static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";

    private static final String FIRST_NAME = "some name";

    private static final String LAST_NAME = "last name";

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    OrganisationInternalControllerV2 organisationInternalControllerV2;

    @Autowired
    UserProfileFeignClient userProfileFeignClient;

    @MockBean
    PaymentAccountService paymentAccountService;


    @MockBean
    OrganisationServiceImpl organisationService;


    public static final String ORG_NAME = "Org-Name";
    public static final String SRA_ID = "sra-id";
    public static final String COMPANY_NUMBER = "companyN";
    public static final String COMPANY_URL = "www.org.com";

    @MockBean
    OrganisationResponse  organisationResponse;

    @MockBean
    OrganisationOtherOrgsCreationRequest  organisationOtherOrgsCreationRequest;

    @Override
    void setController() {
        testTarget.setControllers(organisationInternalControllerV2);
    }


    @State("a request to register an internal organisationV2")
    public void toRegisterNewOrganisation() {
        Organisation org = getCreateOrganisationResponse();
        OrganisationResponse response = new OrganisationResponse(org);
        when(organisationService.createOrganisationFrom(any())).thenReturn(response);

    }


    //retrieveOrganisations
    @State("Organisation V2 exists for given Id")
    public void setUpOrganisationForGivenId() {
        Organisation organisation = getOrganisation();
        mockOrgDetails(organisation);
        when(organisationRepository.findByOrganisationIdentifier(anyString())).thenReturn(organisation);
    }

    //retrieveOrganisations by PBA
    @State("Organisations V2 with payment accounts exist for given Pba Email")
    public void setUpOrganisationWithStatusForGivenPbaEmail() {
        Organisation organisation = getOrganisationWithPbaEmail();
        mockPbaDetails(organisation);
        when(organisationRepository.findByPbaStatus(any())).thenReturn(List.of(organisation));
    }

    @State("An internal OrganisationV2 exists for update")
    public void setUpOrganisationForUpdate() {

        Organisation organisation = new Organisation(ORG_NAME,
            OrganisationStatus.PENDING, SRA_ID, COMPANY_NUMBER,
            false, COMPANY_URL);
        addSuperUser(organisation);
        Organisation updatedOrganisation = new Organisation(ORG_NAME,OrganisationStatus.ACTIVE, SRA_ID, COMPANY_NUMBER,
            false, COMPANY_URL);
        when(organisationService.getOrganisationByOrgIdentifier(any())).thenReturn(organisation);
        when(organisationRepository.findByOrganisationIdentifier(anyString())).thenReturn(organisation);
        when(organisationRepository.save(any())).thenReturn(updatedOrganisation);

    }

    @State("Active organisationsV2 exists for a logged in user using lastUpdatedSince")
    public void setActiveOrganisationsForLoggedInUserUsingLastUpdatedSince() throws IOException {

        Organisation organisation = new Organisation(ORG_NAME, OrganisationStatus.ACTIVE, SRA_ID,
                COMPANY_NUMBER, false, COMPANY_URL);
        addSuperUser(organisation);
        organisation.setOrganisationIdentifier("M0AEAP0");
        organisation.setLastUpdated(LocalDateTime.of(2023, 11, 20, 15, 51, 33));
        organisation.setDateApproved(LocalDateTime.of(2023, 11, 19, 15, 51, 33));

        when(organisationRepository.findByLastUpdatedGreaterThanEqual(any()))
                .thenReturn(List.of(organisation));

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        ProfessionalUser profile = new ProfessionalUser("firstName", "lastName",
                "email@org.com", organisation);

        ProfessionalUsersResponse userProfileResponse = new ProfessionalUsersResponse(profile);
        userProfileResponse.setUserIdentifier(UUID.randomUUID().toString());
        userProfiles.add(userProfileResponse);
        professionalUsersEntityResponse.getUsers().addAll(userProfiles);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);

        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());
    }

    @State("Active organisations V2 exists for a logged in user using lastUpdatedSince")
    public void setActiveOrganisationsV2ForLoggedInUserUsingLastUpdatedSince() throws IOException {

        Organisation organisation = new Organisation(ORG_NAME, OrganisationStatus.ACTIVE, SRA_ID,
                COMPANY_NUMBER, false, COMPANY_URL);
        addSuperUser(organisation);
        organisation.setOrganisationIdentifier("M0AEAP0");
        organisation.setLastUpdated(LocalDateTime.of(2023, 11, 20, 15, 51, 33));
        organisation.setDateApproved(LocalDateTime.of(2023, 11, 19, 15, 51, 33));

        when(organisationRepository.findByLastUpdatedGreaterThanEqual(any()))
                .thenReturn(List.of(organisation));

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        ProfessionalUser profile = new ProfessionalUser("firstName", "lastName",
                "email@org.com", organisation);

        ProfessionalUsersResponse userProfileResponse = new ProfessionalUsersResponse(profile);
        userProfileResponse.setUserIdentifier(UUID.randomUUID().toString());
        userProfiles.add(userProfileResponse);
        professionalUsersEntityResponse.getUsers().addAll(userProfiles);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);

        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(Response.builder()
                .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());
    }

    private void mockOrgDetails(Organisation organisation) {
        List<Organisation> organisationList = new ArrayList<>();
        organisationList.add(organisation);
        when(organisationService.retrieveAllOrganisationsForV2Api(null, null))
            .thenReturn(new OrganisationsDetailResponseV2(
                organisationList, true, true,
                false,true));
    }

    private void addSuperUser(Organisation organisation) {
        SuperUser superUser = new SuperUser(FIRST_NAME, LAST_NAME,
                ORGANISATION_EMAIL, organisation);
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
        ContactInformation contactInformation = getContactInformation();
        organisation.setContactInformations(List.of(contactInformation));
        return organisation;
    }

    @NotNull
    private static ContactInformation getContactInformation() {
        ContactInformation contactInformation = new ContactInformation();
        contactInformation.setUprn("uprn");
        contactInformation.setAddressLine1("addressLine1");
        contactInformation.setAddressLine2("addressLine2");
        contactInformation.setCountry("country");
        contactInformation.setPostCode("HA5 1BJ");
        contactInformation.setCreated(LocalDateTime.now());
        contactInformation.setId(UUID.randomUUID());
        return contactInformation;
    }


    private Organisation getCreateOrganisationResponse() {
        Organisation organisation = new Organisation(ORG_NAME, OrganisationStatus.PENDING, SRA_ID,
            COMPANY_NUMBER, false, COMPANY_URL);
        organisation.setSraRegulated(true);
        organisation.setOrgType("123");
        OrgAttribute orgAttribute = new OrgAttribute();
        orgAttribute.setKey("123");
        orgAttribute.setValue("ACCA");
        organisation.setOrgAttributes(List.of(orgAttribute));
        organisation.setOrganisationIdentifier("AAA6");
        ContactInformation contactInformation = getContactInformation();
        organisation.setContactInformations(List.of(contactInformation));
        return organisation;
    }


    private void mockPbaDetails(Organisation organisation) {
        when(paymentAccountService.findPaymentAccountsByEmail(ORGANISATION_EMAIL))
            .thenReturn(organisation);
    }

    private Organisation getOrganisationWithPbaEmail() {
        Organisation organisation = new Organisation(ORG_NAME, OrganisationStatus.ACTIVE,
            SRA_ID,COMPANY_NUMBER, true, COMPANY_URL);
        SuperUser su = getSuperUser();
        organisation.setUsers(Arrays.asList(su));

        PaymentAccount pa = new PaymentAccount();
        pa.setPbaNumber("pbaNumber");
        pa.setPbaStatus(PbaStatus.ACCEPTED);
        organisation.setPaymentAccounts(Arrays.asList(pa));

        ContactInformation contactInformation = getContactInformation();
        organisation.setContactInformations(List.of(contactInformation));

        organisation.setOrgType("123");
        OrgAttribute orgAttribute = new OrgAttribute();
        orgAttribute.setKey("123");
        orgAttribute.setValue("ACCA");
        organisation.setOrgAttributes(List.of(orgAttribute));

        return organisation;
    }

    @NotNull
    private static SuperUser getSuperUser() {
        SuperUser su = new SuperUser();
        su.setEmailAddress(ORGANISATION_EMAIL);
        su.setFirstName("some-fname");
        su.setLastName("some-lname");
        su.setUserIdentifier("someUserIdentifier");
        return su;
    }
}
