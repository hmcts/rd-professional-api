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
import uk.gov.hmcts.reform.professionalapi.controller.external.OrganisationExternalController;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.PbaStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrgAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.MfaStatusService;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.fromString;
import static uk.gov.hmcts.reform.professionalapi.util.RefDataUtil.randomUUID;

@Provider("referenceData_organisationalExternalPbas")
@Import(OrganisationalExternalControllerProviderTestConfiguration.class)
public class OrganisationalExternalControllerProviderTest extends MockMvcProviderTest {

    private static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";
    private static final String USER_JWT = "Bearer 8gf364fg367f67";

    @Autowired
    ProfessionalUserRepository professionalUserRepositoryMock;

    @Autowired
    OrganisationExternalController organisationExternalController;

    @Autowired
    UserProfileFeignClient userProfileFeignClientMock;

    @Autowired
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverterMock;

    @Autowired
    IdamRepository idamRepositoryMock;

    @Autowired
    MfaStatusService mfaStatusService;

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
    OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImplMock;

    @Autowired
    PaymentAccountValidator paymentAccountValidatorMock;

    @Autowired
    ContactInformationRepository contactInformationRepositoryMock;

    @Mock
    Authentication authentication;
    @Mock
    SecurityContext securityContext;

    @Autowired
    OrgAttributeRepository orgAttributeRepository;

    @Override
    void setController() {
        testTarget.setControllers(organisationExternalController);
    }

    @State({"Pbas organisational data exists for identifier " + ORGANISATION_EMAIL})
    public void toRetreiveOrganisationalDataForIdentifier() throws IOException {

        Jwt jwt =   Jwt.withTokenValue(USER_JWT)
                .claim("aClaim", "aClaim")
                .claim("aud", Lists.newArrayList("ccd_gateway"))
                .header("aHeader", "aHeader")
                .build();
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication().getPrincipal()).thenReturn(jwt);

        String name = "name";
        String sraId = "sraId";
        String companyNumber = "companyNumber";
        String companyUrl = "companyUrl";

        ProfessionalUser professionalUser = getProfessionalUser(name, sraId, companyNumber, companyUrl);

        UserProfile profile = new UserProfile(randomUUID(), "email@org.com",
                "firstName", "lastName", IdamStatus.ACTIVE);

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);
        String body = objectMapper.writeValueAsString(userProfileResponse);

        when(userProfileFeignClientMock.getUserProfileById("someUserIdentifier"))
                .thenReturn(Response.builder()
                        .request(mock(Request.class))
                        .body(body, Charset.defaultCharset()).status(200).build());

        when(idamRepositoryMock.getUserInfo(anyString()))
                .thenReturn(UserInfo.builder().roles(Arrays.asList("pui-finance-manager")).build());

        when(professionalUserRepositoryMock.findByEmailAddress(ORGANISATION_EMAIL)).thenReturn(professionalUser);

    }

    @State({"Delete payment accounts of an active organisation"})
    public void toDeletePaymentAccountsOfAnOrganisation() throws IOException {

        doNothing().when(professionalUserServiceMock).checkUserStatusIsActiveByUserId(any());

        when(organisationServiceMock.getOrganisationByOrgIdentifier(any()))
                .thenReturn(organisationMock);

        PaymentAccount paymentAccount = new PaymentAccount();
        paymentAccount.setPbaNumber("PBA0000001");
        when(organisationMock.getOrganisationIdentifier()).thenReturn("someIdentifier");
        when(organisationMock.getPaymentAccounts()).thenReturn(List.of(paymentAccount));

        when(paymentAccountRepositoryMock.findByPbaNumberIn(anySet())).thenReturn(List.of(paymentAccount));
        doNothing().when(paymentAccountRepositoryMock).deleteByPbaNumberUpperCase(anySet());
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
        su.setUserIdentifier(fromString("someUserIdentifier"));

        PaymentAccount pa = new PaymentAccount();
        pa.setPbaNumber("pbaNumber");
        pa.setPbaStatus(PbaStatus.ACCEPTED);

        organisation.setPaymentAccounts(Arrays.asList(pa));
        organisation.setUsers(Arrays.asList(su));

        ProfessionalUser pu = new ProfessionalUser();
        pu.setEmailAddress(ORGANISATION_EMAIL);
        pu.setOrganisation(organisation);
        return pu;

    }

    @State({"Add payment accounts of an active organisation"})
    public void toAddPaymentAccountsOfAnOrganisation()  {

        when(organisationServiceMock.getOrganisationByOrgIdentifier(any()))
                .thenReturn(organisationMock);
        doNothing().when(organisationIdentifierValidatorImplMock).validateOrganisationIsActive(any(), any());
        doNothing().when(professionalUserServiceMock).checkUserStatusIsActiveByUserId(any());
        doNothing().when(paymentAccountValidatorMock).checkPbaNumberIsValid(any(), any());
        doNothing().when(paymentAccountValidatorMock).getDuplicatePbas(any());

        PaymentAccount paymentAccount = new PaymentAccount();
        paymentAccount.setPbaNumber("PBA0000001");
        when(organisationMock.getOrganisationIdentifier()).thenReturn("someIdentifier");

        when(paymentAccountRepositoryMock.save(any(PaymentAccount.class))).thenReturn(paymentAccount);

    }

    @State({"Add contact informations to organisation"})
    public void toAddContactInformationsToOrganisation() {
        when(organisationServiceMock.getOrganisationByOrgIdentifier(any()))
                .thenReturn(organisationMock);

        when(organisationMock.getOrganisationIdentifier()).thenReturn("someIdentifier");
        doNothing().when(organisationServiceMock).addContactInformationsToOrganisation(any(),any());

    }

    @State({"Delete Multiple Addresses of an active organisation"})
    public void toDeleteMultipleAddressesOfOrganisation() throws IOException {

        when(organisationServiceMock.getOrganisationByOrgIdentifier(any()))
                .thenReturn(organisationMock);
        ContactInformation contactInformation01 = new ContactInformation();
        contactInformation01.setAddressLine1("addressLine1");
        contactInformation01.setId(UUID.randomUUID());
        ContactInformation contactInformation02 = new ContactInformation();
        contactInformation02.setAddressLine1("addressLine2");
        contactInformation02.setId(UUID.randomUUID());

        when(organisationMock.getContactInformation()).thenReturn(
                Arrays.asList(contactInformation01, contactInformation02));
        doNothing().when(organisationIdentifierValidatorImplMock).validateOrganisationIsActive(any(), any());
        doNothing().when(professionalUserServiceMock).checkUserStatusIsActiveByUserId(any());

        when(organisationMock.getOrganisationIdentifier()).thenReturn("someIdentifier");

        doNothing().when(contactInformationRepositoryMock).deleteByIdIn(anySet());
    }
}
