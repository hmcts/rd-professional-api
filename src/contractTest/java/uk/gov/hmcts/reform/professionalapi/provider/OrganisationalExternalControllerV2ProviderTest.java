package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import feign.Request;
import feign.Response;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponseV2;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.OrgAttribute;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.UserAttributeService;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Provider("referenceData_organisationalExternalPbasV2")
@Import(OrganisationalExternalControllerV2ProviderTestConfiguration.class)
public class OrganisationalExternalControllerV2ProviderTest extends MockMvcProviderTest {

    public static final String A_CLAIM = "aClaim";
    public static final String A_HEADER = "aHeader";
    public static final String ORG_NAME = "Org-Name";
    public static final String SRA_ID = "sra-id";
    public static final String COMPANY_NUMBER = "companyN";
    public static final String COMPANY_URL = "www.org.com";
    private static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";
    private static final String USER_JWT = "Bearer 8gf364fg367f67";
    private static final String SOME_USER_IDENTIFIER = "someUserIdentifier";
    @Autowired
    OrganisationExternalControllerV2 organisationExternalControllerV2;


    @MockBean
    OrganisationRepository organisationRepository;

    @MockBean
    Authentication authentication;
    @MockBean
    SecurityContext securityContext;
    @Autowired
    OrganisationServiceImpl organisationServiceImpl;
    @Autowired
    UserProfileFeignClient userProfileFeignClientMock;
    @Autowired
    ProfessionalUserRepository professionalUserRepositoryMock;
    @Autowired
    IdamRepository idamRepositoryMock;

    @Autowired
    UserAttributeRepository userAttributeRepository;

    @Autowired
    UserAttributeService userAttributeService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Organisation organisationMock;

    @Override
    void setController() {
        testTarget.setControllers(organisationExternalControllerV2);
    }

    @State("Pbas External organisational v2 data exists for identifier")
    public void toRetreiveOrganisationalDataForIdentifier() throws IOException {

        Jwt jwt = Jwt.withTokenValue(USER_JWT)
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
            .thenReturn(UserInfo.builder().roles(List.of("pui-finance-manager")).build());

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

        organisation.setPaymentAccounts(List.of(pa));
        organisation.setUsers(List.of(su));

        organisation.setOrgAttributes(List.of(oa));
        organisation.setOrgType("some");

        ProfessionalUser pu = new ProfessionalUser();
        pu.setEmailAddress(ORGANISATION_EMAIL);
        pu.setOrganisation(organisation);
        return pu;

    }


    @State({"a request to register an External organisationV2"})
    public void toRegisterNewOrganisation() {

        Organisation org = getCreateOrganisationResponse();
        OrganisationResponse response = new OrganisationResponse(org);
        when(organisationServiceImpl.createOrganisationFrom(any())).thenReturn(response);


    }


    @State({"OrganisationV2 External with Id exists"})
    public void toRetreiveOrganisationalData() {

        setUpOrganisation("name", "SRdid", "123", "url");

        OrganisationEntityResponseV2 organisationEntityResponseV2 = new
            OrganisationEntityResponseV2(organisationMock, true,
            true,
            true,
            true);

        when(organisationServiceImpl.retrieveOrganisationForV2Api(any(), anyBoolean()))
            .thenReturn(organisationEntityResponseV2);

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
        organisationMock.setContactInformations(List.of(contactInformation));

        organisationMock.setOrgType("123");
        OrgAttribute orgAttribute = new OrgAttribute();
        orgAttribute.setKey("123");
        orgAttribute.setValue("ACCA");
        organisationMock.setOrgAttributes(List.of(orgAttribute));
    }

    private Organisation getCreateOrganisationResponse() {
        Organisation organisation = new Organisation(ORG_NAME, OrganisationStatus.PENDING, SRA_ID,
            COMPANY_NUMBER, false, COMPANY_URL);
        organisation.setSraRegulated(true);
        organisation.setOrgType("123");
        OrgAttribute orgAttribute = new OrgAttribute();
        orgAttribute.setKey("123");
        orgAttribute.setValue("ACCA");
        organisation.setOrgAttributes(List.of(orgAttribute));
        organisation.setOrganisationIdentifier("AAA6");
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
}
