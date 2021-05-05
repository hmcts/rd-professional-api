package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.internal.OrganisationInternalController;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PbaResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.service.MfaStatusService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Provider("referenceData_organisationalInternal")
@Import(OrganisationalInternalControllerProviderTestConfiguration.class)
public class OrganisationalInternalControllerProviderTest extends MockMvcProviderTest {
    @Autowired
    OrganisationRepository organisationRepository;

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

    @Override
    void setController() {
        testTarget.setControllers(organisationInternalController);
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

    @State("Active organisations exists for a logged in user")
    public void setActiveOrganisationsForLoggedInUser() throws IOException {

        Organisation organisation = new Organisation("Org-Name", OrganisationStatus.ACTIVE, "sra-id",
            "companyN", false, "www.org.com");
        addSuperUser(organisation);

        when(organisationRepository.findByStatus(OrganisationStatus.ACTIVE)).thenReturn(Arrays.asList(organisation));

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();
        ProfessionalUser profile = new ProfessionalUser("firstName", "lastName",
            "email@org.com", organisation);

        ProfessionalUsersResponse userProfileResponse = new ProfessionalUsersResponse(profile);
        userProfileResponse.setUserIdentifier(UUID.randomUUID().toString());
        userProfiles.add(userProfileResponse);
        professionalUsersEntityResponse.getUserProfiles().addAll(userProfiles);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false);
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);

        when(userProfileFeignClient.getUserProfiles(any(), any(), any())).thenReturn(Response.builder()
            .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());
    }

    @State("User invited to Organisation")
    public void setUpUserForInviteToOrganisation() throws IOException {

        Organisation organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
            "companyN", false, "www.org.com");
        organisation.setOrganisationIdentifier("UTVC86X");

        ProfessionalUser pu = new ProfessionalUser("firstName", "lastName",
            "email@org.com", organisation);
        when(professionalUserService
            .findProfessionalUserByEmailAddress(anyString())).thenReturn(pu);

        when(prdEnumService.getPrdEnumByEnumType(any())).thenReturn(Arrays.asList("role"));

        UserProfileCreationResponse userProfileCreationResponse = getUserProfileCreationResponse();

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileCreationResponse);

        when(userProfileFeignClient.createUserProfile(any(UserProfileCreationRequest.class)))
            .thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset())
                .status(201).build());
    }

    @State("An Organisation exists for update")
    public void setUpOrganisationForUpdate() {

        Organisation organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
            "companyN", false, "www.org.com");
        addSuperUser(organisation);

        when(organisationRepository.findByOrganisationIdentifier(anyString())).thenReturn(organisation);

    }

    @State("An Organisation with PBA accounts exists")
    public void setUpOrganisationForPBAsUpdate() {

        Organisation organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
            "companyN", false, "www.org.com");
        addSuperUser(organisation);

        when(organisationRepository.findByOrganisationIdentifier(anyString())).thenReturn(organisation);

        when(paymentAccountService.editPaymentAccountsByOrganisation(any(Organisation.class),
            any(PbaEditRequest.class)))
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
        Organisation organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
            "companyN", false, "www.org.com");
        organisation.setSraRegulated(true);
        organisation.setOrganisationIdentifier("someOrganisationIdentifier");
        ContactInformation contactInformation = new ContactInformation();
        contactInformation.setAddressLine1("addressLine1");
        contactInformation.setAddressLine2("addressLine2");
        contactInformation.setCountry("country");
        contactInformation.setPostCode("HA5 1BJ");
        organisation.setContactInformations(Arrays.asList(contactInformation));
        return organisation;
    }

    private UserProfileCreationResponse getUserProfileCreationResponse() {
        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId(UUID.randomUUID().toString());
        userProfileCreationResponse.setIdamRegistrationResponse(201);
        return userProfileCreationResponse;
    }
}