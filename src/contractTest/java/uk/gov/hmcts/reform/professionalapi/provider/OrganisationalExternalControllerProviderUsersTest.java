package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.WebMvcProviderTest;
import uk.gov.hmcts.reform.professionalapi.configuration.WebConfig;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.external.OrganisationExternalController;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.UserProfileCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserAccessType;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.professionalapi.pact.util.PactUtils;
import uk.gov.hmcts.reform.professionalapi.repository.BulkCustomerDetailsRepository;
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserConfiguredAccessRepository;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.ACTIVE;

@Provider("referenceData_organisationalExternalUsers")
@WebMvcTest({OrganisationExternalController.class})
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {OrganisationalExternalControllerProviderUsersTestConfiguration.class, WebConfig.class})
public class OrganisationalExternalControllerProviderUsersTest extends WebMvcProviderTest {

    private static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";
    private static final String USER_JWT = "Bearer some-access-token";
    @Autowired
    ProfessionalUserRepository professionalUserRepositoryMock;

    @Autowired
    ProfessionalUserService professionalUserServiceMock;

    @Autowired
    OrganisationExternalController organisationExternalController;

    @Autowired
    UserAttributeRepository userAttributeRepository;

    @Autowired
    OrganisationRepository organisationRepository;

    @Autowired
    UserConfiguredAccessRepository userConfiguredAccessRepository;

    @Autowired
    BulkCustomerDetailsRepository bulkCustomerDetailsRepository;

    @Autowired
    UserProfileFeignClient userProfileFeignClientMock;

    @Autowired
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverterMock;

    @Autowired
    IdamRepository idamRepositoryMock;

    @Mock
    Authentication authentication;
    @Mock
    SecurityContext securityContext;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Organisation organisation;


    @State({"a request to register an organisation"})
    public void toRegisterNewOrganisation() throws IOException {

        when(organisationRepository.save(any(Organisation.class))).thenAnswer(i -> i.getArguments()[0]);

        when(professionalUserRepositoryMock.save(any(ProfessionalUser.class))).then((i -> i.getArguments()[0]));
    }

    @State({"Organisation exists that can invite new users"})
    public void toInviteNewUsers() throws IOException {
        mockSecurityContext();
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
        when(professionalUserServiceMock.findProfessionalUserByEmailAddress(any()))
                .thenReturn(professionalUser);

        when(organisationRepository.findByOrganisationIdentifier("someOrganisationIdentifier"))
                .thenReturn(organisation);


        setUpUserProfileClientInteraction();
    }

    @State({"Organisation exists that can invite new users with AccessTypes"})
    public void toInviteNewUsersWithAccessTypes() throws IOException {
        mockSecurityContext();
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
        when(professionalUserServiceMock.findProfessionalUserByEmailAddress(any()))
                .thenReturn(professionalUser);

        when(organisationRepository.findByOrganisationIdentifier("someOrganisationIdentifier"))
                .thenReturn(organisation);


        Set<UserAccessType> userAccessTypes = PactUtils.getUserAccessTypes();
        verify(professionalUserServiceMock).saveAllUserAccessTypes(professionalUser, userAccessTypes);

        setUpUserProfileClientInteraction();
    }

    @State({"Organisation with Id exists"})
    public void toRetreiveOrganisationalDataForIdentifier() throws IOException {
        mockSecurityContext();

        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "email@org.com",
                "firstName", "lastName", IdamStatus.ACTIVE);
        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);
        String body = objectMapper.writeValueAsString(userProfileResponse);

        when(userProfileFeignClientMock.getUserProfileById("someUserIdentifier"))
                .thenReturn(Response.builder()
                        .request(mock(Request.class))
                        .body(body, Charset.defaultCharset()).status(200).build());

        when(professionalUserRepositoryMock.findByUserIdentifier("someUid")).thenReturn(
                setUpProfessionalUser());

        when(organisationRepository.findByOrganisationIdentifier("someOrganisationIdentifier"))
                .thenReturn(organisation);

    }

    private void mockSecurityContext() {
        Jwt jwt =   Jwt.withTokenValue(USER_JWT)
                .claim("aClaim", "aClaim")
                .claim("tokenName", "access_token")
                .claim("aud", Collections.singletonList("pui-case-manager"))
                .header("aHeader", "aHeader")
                .build();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication().getPrincipal()).thenReturn(jwt);
        when(idamRepositoryMock.getUserInfo(anyString()))
                .thenReturn(UserInfo.builder().uid("someUid")
                        .roles(Arrays.asList("pui-case-manager")).build());
    }

    @State({"Organisations exists with status of Active"})
    public void toRetrieveActiveOrganisations() throws IOException {

        ProfessionalUser professionalUser = setUpProfessionalUser();
        when(professionalUserRepositoryMock.findByUserIdentifier("someUid")).thenReturn(professionalUser);
        when(professionalUserServiceMock.findProfessionalUserByEmailAddress(any()))
                .thenReturn(professionalUser);
        when(organisationRepository.findByStatus(ACTIVE)).thenReturn(asList(organisation));

    }


    private void setUpUserProfileClientInteraction() throws JsonProcessingException {
        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse();
        userProfileCreationResponse.setIdamId(UUID.randomUUID().toString());
        userProfileCreationResponse.setIdamRegistrationResponse(201);

        ObjectMapper mapper = new ObjectMapper();
        String bodyUp = mapper.writeValueAsString(userProfileCreationResponse);

        when(userProfileFeignClientMock.createUserProfile(any(UserProfileCreationRequest.class)))
                .thenReturn(Response.builder().request(mock(Request.class)).body(bodyUp, Charset.defaultCharset())
                        .status(201).build());
    }


    private ProfessionalUser setUpProfessionalUser(String name, String sraId, String companyNumber, String companyUrl) {
        organisation = PactUtils.setUpOrganisation(name, sraId, companyNumber, companyUrl);

        SuperUser su = new SuperUser();
        su.setEmailAddress("superUser@email.com");
        su.setFirstName("some-fname");
        su.setLastName("some-lname");
        su.setUserIdentifier("someUserIdentifier");

        PaymentAccount pa = new PaymentAccount();
        pa.setPbaNumber("pbaNumber");

        organisation.setPaymentAccounts(asList(pa));
        organisation.setUsers(asList(su));

        ProfessionalUser pu = new ProfessionalUser();
        pu.setEmailAddress(ORGANISATION_EMAIL);
        pu.setOrganisation(organisation);
        return pu;

    }

    private ProfessionalUser setUpProfessionalUser() {
        String name = "name";
        String sraId = "sraId";
        String companyNumber = "companyNumber";
        String companyUrl = "companyUrl";

        return setUpProfessionalUser(name, sraId, companyNumber, companyUrl);
    }

}
