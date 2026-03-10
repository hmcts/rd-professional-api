package uk.gov.hmcts.reform.professionalapi;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.professionalapi.configuration.ApplicationConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.ProfessionalUserReqValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UpdateOrganisationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationByProfileIdsRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.UsersInOrganisationsByOrganisationIdentifiersRequestValidator;
import uk.gov.hmcts.reform.professionalapi.repository.BulkCustomerDetailsRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ContactInformationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrgAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserConfiguredAccessRepository;
import uk.gov.hmcts.reform.professionalapi.service.FeatureToggleService;
import uk.gov.hmcts.reform.professionalapi.service.MfaStatusService;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;
import uk.gov.hmcts.reform.professionalapi.service.PaymentAccountService;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;
import uk.gov.hmcts.reform.professionalapi.service.ProfessionalUserService;
import uk.gov.hmcts.reform.professionalapi.service.UserAccountMapService;

@ExtendWith(SpringExtension.class)
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}",
    host = "${PACT_BROKER_URL:localhost}",
    port = "${PACT_BROKER_PORT:80}", consumerVersionSelectors = {
    @VersionSelector(tag = "master")})
@IgnoreNoPactsToVerify
@SuppressWarnings("checkstyle:Indentation")
public abstract class BaseProviderTest {

    @MockitoBean
    protected ApplicationConfiguration configuration;
    @MockitoBean
    protected UserProfileFeignClient userProfileFeignClient;
    @MockitoBean
    protected EntityManagerFactory emf;
    @MockitoBean(name = "jpaMappingContext")
    protected JpaMetamodelMappingContext jpaMetamodelMappingContext;
    @MockitoBean
    protected ProfessionalUserRepository professionalUserRepository;
    @MockitoBean
    protected OrganisationService organisationService;
    @MockitoBean
    protected MfaStatusService mfaStatusService;
    @MockitoBean
    protected ProfessionalUserService professionalUserService;
    @MockitoBean
    protected FeatureToggleService featureToggleService;
    @MockitoBean
    protected PaymentAccountService paymentAccountService;
    @MockitoBean
    protected UserAccountMapService userAccountMapService;
    @MockitoBean
    protected PrdEnumService prdEnumService;
    @MockitoBean
    protected UpdateOrganisationRequestValidator updateOrganisationRequestValidator;
    @MockitoBean
    protected OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImpl;
    @MockitoBean
    protected OrganisationByProfileIdsRequestValidator organisationByProfileIdsRequestValidator;
    @MockitoBean
    protected UsersInOrganisationsByOrganisationIdentifiersRequestValidator usersInOrgValidator;
    @MockitoBean
    protected ProfessionalUserReqValidator profExtUsrReqValidator;
    @MockitoBean
    protected PaymentAccountValidator paymentAccountValidator;
    @MockitoBean
    protected OrganisationCreationRequestValidator organisationCreationRequestValidatorMock;
    @MockitoBean
    protected IdamRepository idamRepository;
    @MockitoBean
    protected OrgAttributeRepository orgAttributeRepository;
    @MockitoBean
    protected ContactInformationRepository contactInformationRepository;
    @MockitoBean
    protected BulkCustomerDetailsRepository bulkCustomerDetailsRepository;
    @MockitoBean
    protected PaymentAccountRepository paymentAccountRepository;
    @MockitoBean
    protected UserConfiguredAccessRepository userConfiguredAccessRepository;
    @MockitoBean
    protected OrganisationRepository organisationRepository;

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }
}
