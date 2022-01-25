package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.controller.constants.IdamStatus;
import uk.gov.hmcts.reform.professionalapi.controller.external.OrganisationExternalController;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.response.ContactInformationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.controller.response.GetUserProfileResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.PaymentAccount;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserProfile;
import uk.gov.hmcts.reform.professionalapi.oidc.JwtGrantedAuthoritiesConverter;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Provider("referenceData_organisationalExternalPbas")
@Import(OrganisationalExternalControllerProviderTestConfiguration.class)
public class OrganisationalExternalControllerProviderTest extends MockMvcProviderTest {

    private static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";

    @Autowired
    ProfessionalUserRepository professionalUserRepositoryMock;

    @Autowired
    OrganisationExternalController organisationExternalController;

    @Autowired
    UserProfileFeignClient userProfileFeignClientMock;

    @Autowired
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverterMock;

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

    @MockBean
    public OrganisationCreationRequestValidator organisationCreationRequestValidatorMock;


    @Autowired
    OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImplMock;

    @Autowired
    PaymentAccountValidator paymentAccountValidatorMock;

    @Override
    void setController() {
        testTarget.setControllers(organisationExternalController);
    }

    @State({"Pbas organisational data exists for identifier " + ORGANISATION_EMAIL})
    public void toRetreiveOrganisationalDataForIdentifier() throws IOException {

        String name = "name";
        String sraId = "sraId";
        String companyNumber = "companyNumber";
        String companyUrl = "companyUrl";

        ProfessionalUser professionalUser = getProfessionalUser(name, sraId, companyNumber, companyUrl);

        UserProfile profile = new UserProfile(UUID.randomUUID().toString(), "email@org.com",
                "firstName", "lastName", IdamStatus.ACTIVE);

        GetUserProfileResponse userProfileResponse = new GetUserProfileResponse(profile, false);
        String body = objectMapper.writeValueAsString(userProfileResponse);

        when(userProfileFeignClientMock.getUserProfileById("someUserIdentifier"))
                .thenReturn(Response.builder()
                        .request(mock(Request.class))
                        .body(body, Charset.defaultCharset()).status(200).build());

        when(jwtGrantedAuthoritiesConverterMock.getUserInfo())
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

        ContactInformationEntityResponse contactResponse = new ContactInformationEntityResponse();
        when(organisationServiceMock.addContactInformationsToOrganisation(any(),any())).thenReturn(contactResponse);

    }
}
