package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.external.OrganisationExternalController;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Provider("referenceData_organisationalExternalUsers")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
    host = "${PACT_BROKER_URL:localhost}",
    port = "${PACT_BROKER_PORT:80}",
    consumerVersionSelectors = {@VersionSelector(tag = "${PACT_BRANCH_NAME:Dev}")})
@WebMvcTest({OrganisationExternalController.class})
@AutoConfigureMockMvc(addFilters = false)
@Import(OrganisationalExternalControllerProviderUsersTestConfiguration.class)
@TestPropertySource(locations = "/application-contract.yaml")
public class OrganisationalExternalControllerProviderUsersTest {

    @Autowired
    MockMvc mockMvc;

    private static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";

    @Autowired
    ProfessionalUserRepository professionalUserRepositoryMock;

    @Autowired
    ProfessionalUserService professionalUserServiceMock;

    @Autowired
    OrganisationExternalController organisationExternalController;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    UserProfileFeignClient userProfileFeignClientMock;

    @Autowired
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverterMock;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Organisation organisation;

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @BeforeEach
    void before(PactVerificationContext context) {
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        context.setTarget(new MockMvcTestTarget(mockMvc));

    }

    @State({"a request to register an organisation"})
    public void toRegisterNewOrganisation() throws IOException {

        when(organisationRepository.save(any(Organisation.class))).thenAnswer(i -> i.getArguments()[0]);

        when(professionalUserRepositoryMock.save(any(ProfessionalUser.class))).then((i -> i.getArguments()[0]));
    }

    @State({"Organisation exists that can invite new users"})
    public void toInviteNewUsers() throws IOException {

        ProfessionalUser professionalUser = setUpProfessionalUser();

        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "email@org.com",
            "firstName", "lastName", IdamStatus.ACTIVE);

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);
        String body = objectMapper.writeValueAsString(userProfileResponse);

        when(userProfileFeignClientMock.getUserProfileById("someUserIdentifier"))
            .thenReturn(Response.builder()
                .request(mock(Request.class))
                .body(body, Charset.defaultCharset()).status(200).build());


        when(professionalUserRepositoryMock.findByUserIdentifier("someUid")).thenReturn(professionalUser);
        when(professionalUserServiceMock.findProfessionalUserByEmailAddress("joe.bloggs@mailnesia.com")).thenReturn(professionalUser);

        when(organisationRepository.findByOrganisationIdentifier("someOrganisationIdentifier")).thenReturn(organisation);


        setUpUserProfileClientInteraction();
    }

    @State({"Organisation with Id exists"})
    public void toRetreiveOrganisationalDataForIdentifier() throws IOException {

        ProfessionalUser professionalUser = setUpProfessionalUser();

        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "email@org.com",
            "firstName", "lastName", IdamStatus.ACTIVE);

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);
        String body = objectMapper.writeValueAsString(userProfileResponse);

        when(userProfileFeignClientMock.getUserProfileById("someUserIdentifier"))
            .thenReturn(Response.builder()
                .request(mock(Request.class))
                .body(body, Charset.defaultCharset()).status(200).build());

        when(professionalUserRepositoryMock.findByUserIdentifier("someUid")).thenReturn(professionalUser);

        when(organisationRepository.findByOrganisationIdentifier("someOrganisationIdentifier")).thenReturn(organisation);

    }


    private void setUpUserProfileClientInteraction() throws JsonProcessingException {
        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId(UUID.randomUUID().toString());
        userProfileCreationResponse.setIdamRegistrationResponse(201);
        String userId = UUID.randomUUID().toString();

        ObjectMapper mapper = new ObjectMapper();
        String bodyUp = mapper.writeValueAsString(userProfileCreationResponse);

        when(userProfileFeignClientMock.createUserProfile(any(UserProfileCreationRequest.class)))
            .thenReturn(Response.builder().request(mock(Request.class)).body(bodyUp, Charset.defaultCharset())
                .status(201).build());
    }


    @NotNull
    private ProfessionalUser setUpProfessionalUser(String name, String sraId, String companyNumber, String companyUrl) {
        setUpOrganisation(name, sraId, companyNumber, companyUrl);

        SuperUser su = new SuperUser();
        su.setEmailAddress("superUser@email.com");
        su.setFirstName("some-fname");
        su.setLastName("some-lname");
        su.setUserIdentifier("someUserIdentifier");

        PaymentAccount pa = new PaymentAccount();
        pa.setPbaNumber("pbaNumber");

        organisation.setPaymentAccounts(Arrays.asList(pa));
        organisation.setUsers(Arrays.asList(su));

        ProfessionalUser pu = new ProfessionalUser();
        pu.setEmailAddress(ORGANISATION_EMAIL);
        pu.setOrganisation(organisation);
        return pu;

    }

    private void setUpOrganisation(String name, String sraId, String companyNumber, String companyUrl) {
        organisation = new Organisation();
        organisation.setName(name);
        organisation.setCompanyNumber(companyNumber);
        organisation.setStatus(OrganisationStatus.ACTIVE);
        organisation.setSraId(sraId);
        organisation.setSraRegulated(true);
        organisation.setOrganisationIdentifier("someOrganisationIdentifier");
        organisation.setCompanyUrl(companyUrl);
        ContactInformation contactInformation = new ContactInformation();
        contactInformation.setAddressLine1("addressLine1");
        contactInformation.setAddressLine2("addressLine2");
        contactInformation.setCountry("country");
        contactInformation.setPostCode("HA5 1BJ");
        organisation.setContactInformations(Arrays.asList(contactInformation));
    }

    @NotNull
    private ProfessionalUser setUpProfessionalUser() {
        String name = "name";
        String sraId = "sraId";
        String companyNumber = "companyNumber";
        String companyUrl = "companyUrl";

        return setUpProfessionalUser(name, sraId, companyNumber, companyUrl);
    }
}
