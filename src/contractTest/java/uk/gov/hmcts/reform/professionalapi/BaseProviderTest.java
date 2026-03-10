package uk.gov.hmcts.reform.professionalapi;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.junitsupport.loader.VersionSelector;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
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
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrgAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.PaymentAccountRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.repository.UserConfiguredAccessRepository;
import uk.gov.hmcts.reform.professionalapi.service.FeatureToggleService;
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
    ApplicationConfiguration configuration;
    @MockitoBean
    UserProfileFeignClient userProfileFeignClient;
    @MockitoBean
    EntityManagerFactory emf;
    @MockitoBean
    ProfessionalUserRepository professionalUserRepository;
    @MockitoBean
    OrganisationService organisationService;
    @MockitoBean
    ProfessionalUserService professionalUserService;
    @MockitoBean
    FeatureToggleService featureToggleService;
    @MockitoBean
    PaymentAccountService paymentAccountService;
    @MockitoBean
    UserAccountMapService userAccountMapService;
    @MockitoBean
    PrdEnumService prdEnumService;
    @MockitoBean
    UpdateOrganisationRequestValidator updateOrganisationRequestValidator;
    @MockitoBean
    OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImpl;
    @MockitoBean
    OrganisationByProfileIdsRequestValidator organisationByProfileIdsRequestValidator;
    @MockitoBean
    UsersInOrganisationsByOrganisationIdentifiersRequestValidator usersInOrgValidator;
    @MockitoBean
    ProfessionalUserReqValidator profExtUsrReqValidator;
    @MockitoBean
    PaymentAccountValidator paymentAccountValidator;
    @MockitoBean
    OrganisationCreationRequestValidator organisationCreationRequestValidatorMock;
    @MockitoBean
    IdamRepository idamRepository;
    @MockitoBean
    OrgAttributeRepository orgAttributeRepository;
    @MockitoBean
    PaymentAccountRepository paymentAccountRepository;
    @MockitoBean
    UserConfiguredAccessRepository userConfiguredAccessRepository;
    @MockitoBean
    OrganisationRepository organisationRepository;

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        if (context != null) {
            context.verifyInteraction();
        }
    }
}
