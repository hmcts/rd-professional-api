package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import feign.Request;
import feign.Response;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.external.OrganisationExternalControllerV2;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.OrgAttribute;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrgAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Provider("referenceData_organisationalExternalPbasV2")
@Import(OrganisationalExternalControllerProviderTestConfiguration.class)
public class OrganisationalExternalControllerV2ProviderTest extends MockMvcProviderTest {

    private static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";
    private static final String USER_JWT = "Bearer 8gf364fg367f67";

    private static final String SOME_USER_IDENTIFIER = "someUserIdentifier";

    @Autowired
    ProfessionalUserRepository professionalUserRepositoryMock;

    @Autowired
    OrganisationExternalControllerV2 organisationExternalControllerV2;

    @Autowired
    UserProfileFeignClient userProfileFeignClientMock;

    @Autowired
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverterMock;

    @Autowired
    IdamRepository idamRepositoryMock;
    
    @Autowired
    PaymentAccountRepository paymentAccountRepositoryMock;

    @Autowired
    OrganisationService organisationServiceMock;

    @Autowired
    ProfessionalUserService professionalUserServiceMock;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private Organisation organisationMock;

    @Autowired
    OrganisationRepository organisationRepository;
  
    @Mock
    Authentication authentication;
    @Mock
    SecurityContext securityContext;

    @Autowired
    OrgAttributeRepository orgAttributeRepository;

    @Override
    void setController() {
        testTarget.setControllers(organisationExternalControllerV2);
    }

    public static final String A_CLAIM = "aClaim";
    public static final String A_HEADER = "aHeader";

    @State({"Pbas organisational v2 data exists for identifier " + ORGANISATION_EMAIL})
    public void toRetreiveOrganisationalDataForIdentifier() throws IOException {

        Jwt jwt =   Jwt.withTokenValue(USER_JWT)
                .claim(A_CLAIM, A_CLAIM)
                .claim("aud", Lists.newArrayList("ccd_gateway"))
                .header(A_HEADER, A_HEADER)
                .build();
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication().getPrincipal()).thenReturn(jwt);

        String name = "name";
        String sraId = "sraId";
        String companyNumber = "companyNumber";
        String companyUrl = "companyUrl";

        ProfessionalUser professionalUser = getProfessionalUser(name, sraId, companyNumber, companyUrl);

        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "email@org.com",
                "firstName", "lastName", IdamStatus.ACTIVE);

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);
        String body = objectMapper.writeValueAsString(userProfileResponse);

        when(userProfileFeignClientMock.getUserProfileById(SOME_USER_IDENTIFIER))
                .thenReturn(Response.builder()
                        .request(mock(Request.class))
                        .body(body, Charset.defaultCharset()).status(200).build());

        when(idamRepositoryMock.getUserInfo(anyString()))
                .thenReturn(UserInfo.builder().roles(Arrays.asList("pui-finance-manager")).build());

        when(professionalUserRepositoryMock.findByEmailAddress(ORGANISATION_EMAIL)).thenReturn(professionalUser);

    }



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
        su.setUserIdentifier(SOME_USER_IDENTIFIER);

        PaymentAccount pa = new PaymentAccount();
        pa.setPbaNumber("pbaNumber");
        pa.setPbaStatus(PbaStatus.ACCEPTED);

        OrgAttribute oa = new OrgAttribute();
        oa.setKey("123");
        oa.setValue("ACCA");

        organisation.setPaymentAccounts(Arrays.asList(pa));
        organisation.setUsers(Arrays.asList(su));

        organisation.setOrgAttributes(Arrays.asList(oa));
        organisation.setOrgType("some");

        ProfessionalUser pu = new ProfessionalUser();
        pu.setEmailAddress(ORGANISATION_EMAIL);
        pu.setOrganisation(organisation);
        return pu;

    }


    @State({"a request to register an organisationV2"})
    public void toRegisterNewOrganisation()  {

        when(organisationRepository.save(any(Organisation.class))).thenAnswer(i -> i.getArguments()[0]);

    }



    @State({"OrganisationV2 with Id exists"})
    public void toRetreiveOrganisationalData() throws IOException {
        mockSecurityContext();

        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "email@org.com",
            "firstName", "lastName", IdamStatus.ACTIVE);
        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);
        String body = objectMapper.writeValueAsString(userProfileResponse);

        when(userProfileFeignClientMock.getUserProfileById(SOME_USER_IDENTIFIER))
            .thenReturn(Response.builder()
                .request(mock(Request.class))
                .body(body, Charset.defaultCharset()).status(200).build());

        when(professionalUserRepositoryMock.findByUserIdentifier("someUid")).thenReturn(
            setUpProfessionalUser());

        when(organisationRepository.findByOrganisationIdentifier("someOrganisationIdentifier"))
            .thenReturn(organisationMock);

    }

    private void mockSecurityContext() {
        Jwt jwt =   Jwt.withTokenValue(USER_JWT)
            .claim(A_CLAIM, A_CLAIM)
            .claim("tokenName", "access_token")
            .claim("aud", Collections.singletonList("pui-case-manager"))
            .header(A_HEADER, A_HEADER)
            .build();
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication().getPrincipal()).thenReturn(jwt);
        when(idamRepositoryMock.getUserInfo(anyString()))
            .thenReturn(UserInfo.builder().uid("someUid")
                .roles(Arrays.asList("pui-case-manager")).build());
    }

    private ProfessionalUser setUpProfessionalUser(String name, String sraId, String companyNumber, String companyUrl) {
        setUpOrganisation(name, sraId, companyNumber, companyUrl);

        SuperUser su = new SuperUser();
        su.setEmailAddress("superUser@email.com");
        su.setFirstName("some-fname");
        su.setLastName("some-lname");
        su.setUserIdentifier(SOME_USER_IDENTIFIER);

        PaymentAccount pa = new PaymentAccount();
        pa.setPbaNumber("pbaNumber");

        organisationMock.setPaymentAccounts(asList(pa));
        organisationMock.setUsers(asList(su));
        organisationMock.setOrgType("some");

        OrgAttribute oa = new OrgAttribute();
        oa.setKey("123");
        oa.setValue("ACCA");
        organisationMock.setOrgAttributes(asList(oa));

        ProfessionalUser pu = new ProfessionalUser();
        pu.setEmailAddress(ORGANISATION_EMAIL);
        pu.setOrganisation(organisationMock);
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
        organisationMock = new Organisation();
        organisationMock.setName(name);
        organisationMock.setCompanyNumber(companyNumber);
        organisationMock.setStatus(OrganisationStatus.ACTIVE);
        organisationMock.setSraId(sraId);
        organisationMock.setSraRegulated(true);
        organisationMock.setOrganisationIdentifier("someOrganisationIdentifier");
        organisationMock.setCompanyUrl(companyUrl);
        ContactInformation contactInformation = new ContactInformation();
        contactInformation.setUprn("uprn");
        contactInformation.setAddressLine1("addressLine1");
        contactInformation.setAddressLine2("addressLine2");
        contactInformation.setCountry("country");
        contactInformation.setPostCode("HA5 1BJ");
        organisationMock.setContactInformations(asList(contactInformation));
    }
}
