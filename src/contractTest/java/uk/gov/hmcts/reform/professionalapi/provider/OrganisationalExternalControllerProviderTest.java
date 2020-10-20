package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
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
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Provider("referenceData_organisationalExternalPbas")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",host = "${PACT_BROKER_URL:localhost}", port = "${PACT_BROKER_PORT:80}", consumerVersionSelectors={@VersionSelector(tag ="${PACT_BRANCH_NAME:development}")})
@Import(OrganisationalExternalControllerProviderTestConfiguration.class)
@TestPropertySource(locations = "/application-contract.yaml")
public class OrganisationalExternalControllerProviderTest {

    private static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";

    @Autowired
    ProfessionalUserRepository professionalUserRepositoryMock;

    @Autowired
    OrganisationExternalController organisationExternalController;

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
        testTarget.setControllers(organisationExternalController);
        context.setTarget(testTarget);
    }

    @State({"Pbas organisational data exists for identifier " + ORGANISATION_EMAIL})
    public void toRetreiveOrganisationalDataForIdentifier() throws IOException, JSONException {

        String name = "name";
        String sraId = "sraId";
        String companyNumber = "companyNumber";
        String companyUrl = "companyUrl";

        ProfessionalUser professionalUser = getProfessionalUser(name, sraId, companyNumber, companyUrl);

        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "email@org.com",
            "firstName", "lastName", IdamStatus.ACTIVE);

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);
        String body = objectMapper.writeValueAsString(userProfileResponse);

        when(userProfileFeignClientMock.getUserProfileById("someUserIdentifier")).thenReturn(Response.builder()
            .request(mock(Request.class)).body(body, Charset.defaultCharset()).status(200).build());

        when(jwtGrantedAuthoritiesConverterMock.getUserInfo()).thenReturn(UserInfo.builder().roles(Arrays.asList("pui-finance-manager")).build());

        when(professionalUserRepositoryMock.findByEmailAddress(ORGANISATION_EMAIL)).thenReturn(professionalUser);

    }

    @NotNull
    private ProfessionalUser getProfessionalUser(String name, String sraId, String companyNumber, String companyUrl) {
        ProfessionalUser professionalUser = ProfessionalUserBuilder.aProfessionalUser().withEmailAddress(ORGANISATION_EMAIL)
            .withOrganisation(OrganisationBuilder.anOrganisation().withName(name).withStatus(
                OrganisationStatus.ACTIVE)
                .withSraId(sraId)
                .withSraRegulated(true)
                .withCompanyNumber(companyNumber)
                .withCompanyUrl(companyUrl)
                .withPaymentAccounts(Arrays.asList(PaymentAccountBuilder.aPaymentAccount().withPbaNumber("pbaNumber").build()))
                .withUsers(Arrays.asList(
                    SuperUserBuilder.aSuperUser().withEmailAddress("superUser@email.com").withFirstName("some-fname").withLastName("some-lname")
                        .withUserIdentifier("someUserIdentifier")
                        .withEmailAddress("someone@somewhere.com").build()))
                .build()
            ).build();
        return professionalUser;
    }
}
