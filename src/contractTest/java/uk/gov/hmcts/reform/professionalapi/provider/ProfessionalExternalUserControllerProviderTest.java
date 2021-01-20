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
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.external.OrganisationExternalController;
import uk.gov.hmcts.reform.professionalapi.controller.external.ProfessionalExternalUserController;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ModifyUserRolesResponseTest;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.RoleAdditionResponse;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfileUpdatedData;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Provider("referenceData_professionalExternalUsers")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
    host = "${PACT_BROKER_URL:localhost}",
    port = "${PACT_BROKER_PORT:80}", consumerVersionSelectors = {
    @VersionSelector(tag = "${PACT_BRANCH_NAME:Dev}")})
@Import(ProfessionalExternalUserControllerProviderTestConfiguration.class)
@TestPropertySource(locations = "/application-contract.yaml")
public class ProfessionalExternalUserControllerProviderTest {

    private static final String PROFESSIONAL_USER_ID = "123456";
    private static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";

    @Autowired
    ProfessionalUserRepository professionalUserRepositoryMock;

    @Autowired
    ProfessionalExternalUserController professionalExternalUserController;

    @Autowired
    UserProfileFeignClient userProfileFeignClientMock;

    @Autowired
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverterMock;

    private ObjectMapper objectMapper = new ObjectMapper();

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }


    @BeforeEach
    void before(PactVerificationContext context) {
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        testTarget.setControllers(professionalExternalUserController);
        context.setTarget(testTarget);
    }

    @State({"Professional User exists for identifier " + PROFESSIONAL_USER_ID})
    public void toRetreiveOrganisationalDataForIdentifier() throws IOException, JSONException {

        setupInteractionsForProfessionalUser();
    }

    @State({"Professional User exists for modification with identifier " + PROFESSIONAL_USER_ID })
    public void toUpdateUserRolesForIdentifier() throws IOException, JSONException {

        setupInteractionsForProfessionalUser();
    }

    private void setupInteractionsForProfessionalUser() throws JsonProcessingException {
        String name = "name";
        String sraId = "sraId";
        String companyNumber = "companyNumber";
        String companyUrl = "companyUrl";

        ProfessionalUser professionalUser = getProfessionalUser(name, sraId, companyNumber, companyUrl);
        professionalUser.setEmailAddress("someUserIdentifier");

        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "email@org.com",
            "firstName", "lastName", IdamStatus.ACTIVE);

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);
        String body = objectMapper.writeValueAsString(userProfileResponse);

        when(userProfileFeignClientMock.getUserProfileById("someUserIdentifier"))
            .thenReturn(Response.builder()
                .request(mock(Request.class))
                .body(body, Charset.defaultCharset()).status(200).build());


        when(professionalUserRepositoryMock.findByUserIdentifier(PROFESSIONAL_USER_ID)).thenReturn(professionalUser);
        when(userProfileFeignClientMock.getUserProfileByEmail(anyString())).thenReturn(Response.builder()
            .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        ModifyUserRolesResponse modifyUserRolesResponse = new ModifyUserRolesResponse();
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamMessage("some message");
        roleAdditionResponse.setIdamStatusCode("200");
        modifyUserRolesResponse.setRoleAdditionResponse(roleAdditionResponse);

        String bodyModifyUserRoles = objectMapper.writeValueAsString(modifyUserRolesResponse);

        when(userProfileFeignClientMock.modifyUserRoles(any(), any(), any())).thenReturn(Response.builder()
            .request(mock(Request.class)).body(bodyModifyUserRoles, Charset.defaultCharset()).status(200).build());
    }

    @NotNull
    private ProfessionalUser getProfessionalUser(String name, String sraId, String companyNumber, String companyUrl) {
        Organisation organisation = new Organisation();
        organisation.setName(name);
        organisation.setCompanyNumber(companyNumber);
        organisation.setStatus(OrganisationStatus.ACTIVE);
        organisation.setSraId(sraId);
        organisation.setSraRegulated(true);
        organisation.setCompanyUrl(companyUrl);

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
}
