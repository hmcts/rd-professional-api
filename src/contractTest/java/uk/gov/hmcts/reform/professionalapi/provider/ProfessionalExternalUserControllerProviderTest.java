package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.professionalapi.WebMvcProviderTest;
import uk.gov.hmcts.reform.professionalapi.configuration.WebConfig;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.external.ProfessionalExternalUserController;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.RoleAdditionResponse;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.MfaStatusService;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;

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

@Provider("referenceData_professionalExternalUsers")
@WebMvcTest({ProfessionalExternalUserController.class})
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {ProfessionalExternalUserControllerProviderTestConfiguration.class, WebConfig.class})
public class ProfessionalExternalUserControllerProviderTest extends WebMvcProviderTest {

    private static final String PROFESSIONAL_USER_ID = "123456";
    private static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";

    @Autowired
    ProfessionalUserRepository professionalUserRepositoryMock;

    @Autowired
    OrganisationService organisationServiceMock;

    @Autowired
    ProfessionalExternalUserController professionalExternalUserController;

    @Autowired
    UserProfileFeignClient userProfileFeignClientMock;

    @Autowired
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverterMock;

    @Autowired
    MfaStatusService mfaStatusService;

    private ObjectMapper objectMapper = new ObjectMapper();
    private Organisation organisation;


    @State({"Professional User exists for identifier " + PROFESSIONAL_USER_ID})
    public void toRetreiveOrganisationalDataForIdentifier() throws IOException {
        setupInteractionsForProfessionalUser();
    }

    @State({"Professional User exists for modification with identifier " + PROFESSIONAL_USER_ID})
    public void toUpdateUserRolesForIdentifier() throws IOException {

        setupInteractionsForProfessionalUser();
    }

    @State({"Professional users exist for an Active organisation"})
    public void toRetrieveAllActiveOrganisations() throws IOException {

        setupInteractionsForProfessionalUser();
        when(organisationServiceMock.getOrganisationByOrgIdentifier("someOrganisationIdentifier"))
            .thenReturn(organisation);

        List<ProfessionalUser> users = new ArrayList<>();

        ProfessionalUser profile = new ProfessionalUser("firstName", "lastName",
            "email@org.com", organisation);
        profile.setIdamStatus(IdamStatus.ACTIVE);
        profile.setRoles(Arrays.asList("pui-user-manager", " pui-case-manager"));

        ProfessionalUsersEntityResponse professionalUsersEntityResponse = new ProfessionalUsersEntityResponse();
        List<ProfessionalUsersResponse> userProfiles = new ArrayList<>();

        ProfessionalUsersResponse userProfileResponse = new ProfessionalUsersResponse(profile);
        userProfileResponse.setUserIdentifier(PROFESSIONAL_USER_ID);
        userProfiles.add(userProfileResponse);
        professionalUsersEntityResponse.setUserProfiles(userProfiles);
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false);
        String body = mapper.writeValueAsString(professionalUsersEntityResponse);

        when(userProfileFeignClientMock.getUserProfiles(any(), any(), any())).thenReturn(Response.builder()
            .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());
    }

    private ProfessionalUser setupInteractionsForProfessionalUser() throws JsonProcessingException {
        String name = "name";
        String sraId = "sraId";
        String companyNumber = "companyNumber";
        String companyUrl = "companyUrl";

        ProfessionalUser professionalUser = getProfessionalUser(name, sraId, companyNumber, companyUrl);
        professionalUser.setEmailAddress("someUserIdentifier");

        UserProfile profile = getUserProfile();

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);
        String body = objectMapper.writeValueAsString(userProfileResponse);

        when(userProfileFeignClientMock.getUserProfileById("someUserIdentifier"))
            .thenReturn(Response.builder()
                .request(mock(Request.class))
                .body(body, Charset.defaultCharset()).status(200).build());

        NewUserResponse newUserResponse = new NewUserResponse();
        newUserResponse.setUserIdentifier("a123dfgr46");
        newUserResponse.setIdamStatus("ACTIVE");
        String newUserResponseBody = objectMapper.writeValueAsString(newUserResponse);

        when(userProfileFeignClientMock.getUserProfileByEmail(anyString())).thenReturn(Response.builder()
            .request(mock(Request.class)).body(newUserResponseBody, Charset.defaultCharset()).status(200).build());


        when(professionalUserRepositoryMock.findByUserIdentifier("someUid")).thenReturn(professionalUser);
        when(professionalUserRepositoryMock.findByUserIdentifier(PROFESSIONAL_USER_ID)).thenReturn(professionalUser);
        when(professionalUserRepositoryMock.findByEmailAddress(anyString())).thenReturn(professionalUser);

        when(professionalUserRepositoryMock.findByOrganisation(organisation))
            .thenReturn(Arrays.asList(professionalUser));


        ModifyUserRolesResponse modifyUserRolesResponse = new ModifyUserRolesResponse();
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamMessage("some message");
        roleAdditionResponse.setIdamStatusCode("200");
        modifyUserRolesResponse.setRoleAdditionResponse(roleAdditionResponse);

        String bodyModifyUserRoles = objectMapper.writeValueAsString(modifyUserRolesResponse);

        when(userProfileFeignClientMock.modifyUserRoles(any(), any(), any())).thenReturn(Response.builder()
            .request(mock(Request.class)).body(bodyModifyUserRoles, Charset.defaultCharset()).status(200).build());

        return professionalUser;
    }

    private UserProfile getUserProfile() {
        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "email@org.com",
            "firstName", "lastName", IdamStatus.ACTIVE);
        return profile;
    }

    private ProfessionalUser getProfessionalUser(String name, String sraId, String companyNumber, String companyUrl) {
        organisation = new Organisation();
        organisation.setName(name);
        organisation.setCompanyNumber(companyNumber);
        organisation.setStatus(OrganisationStatus.ACTIVE);
        organisation.setSraId(sraId);
        organisation.setSraRegulated(true);
        organisation.setCompanyUrl(companyUrl);
        organisation.setOrganisationIdentifier("someOrganisationIdentifier");

        SuperUser su = getSuperUser();

        PaymentAccount pa = new PaymentAccount();
        pa.setPbaNumber("pbaNumber");

        organisation.setPaymentAccounts(Arrays.asList(pa));
        organisation.setUsers(Arrays.asList(su));

        ProfessionalUser pu = new ProfessionalUser();
        pu.setUserIdentifier(PROFESSIONAL_USER_ID);
        pu.setEmailAddress(ORGANISATION_EMAIL);
        pu.setOrganisation(organisation);
        return pu;

    }

    private SuperUser getSuperUser() {
        SuperUser su = new SuperUser();
        su.setEmailAddress("superUser@email.com");
        su.setFirstName("some-fname");
        su.setLastName("some-lname");
        su.setUserIdentifier("someUserIdentifier");
        return su;
    }
}
