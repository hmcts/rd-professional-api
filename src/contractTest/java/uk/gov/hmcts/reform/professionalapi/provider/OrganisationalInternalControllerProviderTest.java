package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.internal.OrganisationInternalController;
import uk.gov.hmcts.reform.professionalapi.controller.request.PbaEditRequest;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PbaResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
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

@ExtendWith(SpringExtension.class)
@Provider("referenceData_organisationalInternal")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
    host = "${PACT_BROKER_URL:localhost}",
    port = "${PACT_BROKER_PORT:80}", consumerVersionSelectors = {
    @VersionSelector(tag = "${PACT_BRANCH_NAME:Dev}")})
@Import(OrganisationalInternalControllerProviderTestConfiguration.class)
@TestPropertySource(locations = "/application-contract.yaml")
public class OrganisationalInternalControllerProviderTest {


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

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @BeforeEach
    void before(PactVerificationContext context) {
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        testTarget.setControllers(organisationInternalController);
        context.setTarget(testTarget);
    }

    @State("Users exists for an Organisation")
    public void setUpUsersForOrganisation() throws IOException {

        Organisation organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
            "companyN", false, "www.org.com");
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

        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId(UUID.randomUUID().toString());
        userProfileCreationResponse.setIdamRegistrationResponse(201);

        ObjectMapper mapper = new ObjectMapper();
        String body = mapper.writeValueAsString(userProfileCreationResponse);

        when(userProfileFeignClient.createUserProfile(any(UserProfileCreationRequest.class)))
            .thenReturn(Response.builder().request(mock(Request.class)).body(body, Charset.defaultCharset())
                .status(201).build());
    }


    @State("An Organisation exists for update")
    public void setUpOrganisationForUpdate() throws IOException {

        Organisation organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
            "companyN", false, "www.org.com");
        addSuperUser(organisation);

        when(organisationRepository.findByOrganisationIdentifier(anyString())).thenReturn(organisation);

    }

    @State("An Organisation with PBA accounts exists")
    public void setUpOrganisationForPBAsUpdate() throws IOException {

        Organisation organisation = new Organisation("Org-Name", OrganisationStatus.PENDING, "sra-id",
            "companyN", false, "www.org.com");
        addSuperUser(organisation);

        when(organisationRepository.findByOrganisationIdentifier(anyString())).thenReturn(organisation);

        when(paymentAccountService.editPaymentAccountsByOrganisation(any(Organisation.class), any(PbaEditRequest.class)))
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
}