package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.internal.OrganisationInternalController;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaResponse;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.service.MfaStatusService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.pact.util.PactUtils.getOrgWithMfaStatus;

@Provider("referenceData_organisationalInternal")
@Import(OrganisationalInternalControllerProviderTestConfiguration.class)
public class OrganisationalInternalControllerProviderTest extends MockMvcProviderTest {

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    PaymentAccountRepository paymentAccountRepository;

    @Autowired
    ProfessionalUserService professionalUserService;

    @Autowired
    PrdEnumService prdEnumService;

    @Autowired
    OrganisationInternalController organisationInternalController;

    @Autowired
    UserProfileFeignClient userProfileFeignClient;

    @Autowired
    PaymentAccountService paymentAccountService;

    @Autowired
    MfaStatusService mfaStatusService;

    @Autowired
    MappingJackson2HttpMessageConverter httpMessageConverter;

    @Autowired
    OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImplMock;

    public static final String ORG_NAME = "Org-Name";
    public static final String SRA_ID = "sra-id";
    public static final String COMPANY_NUMBER = "companyN";
    public static final String COMPANY_URL = "www.org.com";

    @Override
    void setController() {
        testTarget.setControllers(organisationInternalController);
        testTarget.setMessageConverters(httpMessageConverter);
    }

    @State("Users exists for an Organisation")
    public void setUpUsersForOrganisation() {

        Organisation organisation = getOrganisation();
        when(organisationRepository.findByOrganisationIdentifier(anyString())).thenReturn(organisation);

    }

    //retrieveOrganisations
    @State("Organisation exists for given Id")
    public void setUpOrganisationForGivenId() {

        Organisation organisation = getOrganisation();
        when(organisationRepository.findByOrganisationIdentifier(anyString())).thenReturn(organisation);

    }

    //retrieveOrganisationsWithPagination
    @State("An organisation exists with pagination")
    public void setUpOrganisationWithPagination() {

        Organisation organisation = getOrganisation();
        Page<Organisation> orgPage = (Page<Organisation>) mock(Page.class);

        when(organisationRepository.findByStatusIn(List.of(OrganisationStatus.ACTIVE, OrganisationStatus.PENDING),
            any(Pageable.class))).thenReturn(orgPage);
        when(orgPage.getContent()).thenReturn(List.of(organisation));
    }


    //retrieveOrganisationsWithStatusAndPagination
    @State("An active organisation exists for given status and with pagination")
    public void setUpOrganisationWithStatusAndPagination() {

        Organisation organisation = new Organisation(ORG_NAME, OrganisationStatus.ACTIVE, SRA_ID,
            COMPANY_NUMBER, false, COMPANY_URL);
        addSuperUser(organisation);
        when(organisationRepository.findByStatusIn(List.of(OrganisationStatus.ACTIVE), any(Pageable.class)))
            .thenReturn(mock(Page.class));
        when(organisationRepository.findByStatusIn(List.of(OrganisationStatus.ACTIVE), any(Pageable.class))
                .getContent()).thenReturn(List.of(organisation));
        when(organisationRepository.findByStatusIn(List.of(OrganisationStatus.ACTIVE), any(Pageable.class)))
            .thenReturn(mock(Page.class));
        when(organisationRepository.findByStatusIn(List.of(OrganisationStatus.ACTIVE), any(Pageable.class))
            .getContent())
            .thenReturn(Collections.emptyList());
    }

    @State("Active organisations exists for a logged in user")
    public void setActiveOrganisationsForLoggedInUser() throws IOException {

        Organisation organisation = new Organisation(ORG_NAME, OrganisationStatus.ACTIVE, SRA_ID,
                COMPANY_NUMBER, false, COMPANY_URL);
        addSuperUser(organisation);

        when(organisationRepository.findByStatus(OrganisationStatus.ACTIVE)).thenReturn(List.of(organisation));

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

    @State("User invited to Organisation")
    public void setUpUserForInviteToOrganisation() throws IOException {

        Organisation organisation = new Organisation(ORG_NAME, OrganisationStatus.PENDING, SRA_ID,
                COMPANY_NUMBER, false, COMPANY_URL);
        organisation.setOrganisationIdentifier("UTVC86X");

        ProfessionalUser pu = new ProfessionalUser("firstName", "lastName",
                "email@org.com", organisation);
        when(professionalUserService
                .findProfessionalUserByEmailAddress(anyString())).thenReturn(pu);

        when(prdEnumService.getPrdEnumByEnumType(any())).thenReturn(List.of("role"));

        UserProfileCreationResponse userProfileCreationResponse = getUserProfileCreationResponse();

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileCreationResponse);

        when(userProfileFeignClient.createUserProfile(any(UserProfileCreationRequest.class)))
                .thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset())
                        .status(201).build());
    }

    @State("An Organisation exists for update")
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
    @State("An Organisation with PBA accounts exists")
    public void setUpOrganisationForPBAsUpdate() {

        Organisation organisation = new Organisation(ORG_NAME, OrganisationStatus.PENDING, SRA_ID,
                COMPANY_NUMBER, false, COMPANY_URL);
        addSuperUser(organisation);

        when(organisationRepository.findByOrganisationIdentifier(anyString())).thenReturn(organisation);

        when(paymentAccountService.editPaymentAccountsByOrganisation(any(Organisation.class),
            any(PbaRequest.class)))
            .thenReturn(new PbaResponse("200", "Success"));
    }

    //MFA put api test
    @State("An Organisation exists with MFA")
    public void setUpOrganisationForMfaUpdate() {
        when(organisationRepository.findByOrganisationIdentifier(anyString())).thenReturn(getOrgWithMfaStatus());
    }

    @SuppressWarnings("checkstyle:AbbreviationAsWordInName")
    @State("Update an Organisation's PBA accounts")
    public void setUpOrganisationForUpdatingPBAs() {
        Organisation organisation = new Organisation("Org-Name", OrganisationStatus.ACTIVE, "sra-id",
                "companyN", false, "www.org.com");

        PaymentAccount paymentAccount = new PaymentAccount("PBA1234567");
        paymentAccount.setOrganisation(organisation);

        doNothing().when(organisationIdentifierValidatorImplMock).validateOrganisationIsActive(any(), any());
        when(organisationRepository.findByOrganisationIdentifier(anyString())).thenReturn(organisation);
        when(paymentAccountRepository.findByPbaNumberIn(anySet())).thenReturn(List.of(paymentAccount));
        when(paymentAccountRepository.saveAll(anyList())).thenReturn(List.of(paymentAccount));
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

    private UserProfileCreationResponse getUserProfileCreationResponse() {
        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId(UUID.randomUUID().toString());
        userProfileCreationResponse.setIdamRegistrationResponse(201);
        return userProfileCreationResponse;
    }

    @State("Organisations with payment accounts exist for given Pba status")
    public void setUpOrganisationWithStatusForGivenPbaStatus() {
        Organisation organisation = getOrganisationWithPbaStatus();
        when(organisationRepository.findByPbaStatus(any())).thenReturn(List.of(organisation));
    }

    private Organisation getOrganisationWithPbaStatus() {
        PaymentAccount paymentAccount = new PaymentAccount();
        paymentAccount.setPbaNumber("PBA12345");
        paymentAccount.setStatusMessage("Approved");
        paymentAccount.setPbaStatus(PbaStatus.ACCEPTED);
        paymentAccount.setCreated(LocalDateTime.now());
        paymentAccount.setLastUpdated(LocalDateTime.now());
        Organisation organisation = new Organisation("Org-Name", OrganisationStatus.ACTIVE, "sra-id",
                "companyN", false, "www.org.com");
        organisation.setSraRegulated(true);
        organisation.setOrganisationIdentifier("org1");
        organisation.setPaymentAccounts(Collections.singletonList(paymentAccount));
        SuperUser superUser = new SuperUser();
        superUser.setFirstName("fName");
        superUser.setLastName("lName");
        superUser.setEmailAddress("example.email@test.com");
        organisation.setUsers(Collections.singletonList(superUser));
        return organisation;
    }
}