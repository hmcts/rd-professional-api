package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.professionalapi.configuration.WebConfig;
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
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.ACTIVE;

@Provider("referenceData_organisationalExternalUsers")
@WebMvcTest({OrganisationExternalController.class})
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {OrganisationalExternalControllerProviderUsersTestConfiguration.class, WebConfig.class})
public class OrganisationalExternalControllerProviderUsersTest extends WebMvcProviderTest {

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
        when(professionalUserServiceMock.findProfessionalUserByEmailAddress("joe.bloggs@mailnesia.com"))
            .thenReturn(professionalUser);

        when(organisationRepository.findByOrganisationIdentifier("someOrganisationIdentifier"))
            .thenReturn(organisation);


        setUpUserProfileClientInteraction();
    }

    @State({"Organisation with Id exists"})
    public void toRetreiveOrganisationalDataForIdentifier() throws IOException {

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

    @State({"Organisations exists with status of Active"})
    public void toRetrieveActiveOrganisations() throws IOException {

        ProfessionalUser professionalUser = setUpProfessionalUser();
        when(professionalUserRepositoryMock.findByUserIdentifier("someUid")).thenReturn(professionalUser);
        when(professionalUserServiceMock.findProfessionalUserByEmailAddress("joe.bloggs@mailnesia.com"))
            .thenReturn(professionalUser);
        when(organisationRepository.findByStatus(ACTIVE)).thenReturn(Arrays.asList(organisation));

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

    private ProfessionalUser setUpProfessionalUser() {
        String name = "name";
        String sraId = "sraId";
        String companyNumber = "companyNumber";
        String companyUrl = "companyUrl";

        return setUpProfessionalUser(name, sraId, companyNumber, companyUrl);
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

}
