package uk.gov.hmcts.reform.professionalapi.provider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import uk.gov.hmcts.reform.professionalapi.configuration.ApplicationConfiguration;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.OrganisationCreationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.PaymentAccountValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.ProfessionalUserReqValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.UpdateOrganisationRequestValidator;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.PrdEnumService;
import uk.gov.hmcts.reform.professionalapi.service.UserAccountMapService;

import javax.persistence.EntityManagerFactory;

public class ProviderTestConfiguration {

    @MockBean
    ApplicationConfiguration configuration;
    @MockBean
    UserProfileFeignClient userProfileFeignClient;
    @MockBean
    EntityManagerFactory emf;
    @MockBean
    ProfessionalUserRepository professionalUserRepository;
    @MockBean
    UserAccountMapService userAccountMapService;

    @MockBean
    protected PrdEnumService prdEnumService;
    @MockBean
    protected UpdateOrganisationRequestValidator updateOrganisationRequestValidator;
    @MockBean
    protected OrganisationCreationRequestValidator organisationCreationRequestValidator;
    @MockBean
    protected OrganisationIdentifierValidatorImpl organisationIdentifierValidatorImpl;
    @MockBean
    protected ProfessionalUserReqValidator profExtUsrReqValidator;
    @MockBean
    protected PaymentAccountValidator paymentAccountValidator;


    @Value("${prd.security.roles.hmcts-admin:}")
    protected String prdAdmin;

    @Value("${prd.security.roles.pui-user-manager:}")
    protected String puiUserManager;

    @Value("${prd.security.roles.pui-organisation-manager:}")
    protected String puiOrgManager;

    @Value("${prd.security.roles.pui-finance-manager}")
    protected String puiFinanceManager;

    @Value("${prd.security.roles.pui-case-manager:}")
    protected String puiCaseManager;

    @Value("${prdEnumRoleType}")
    protected String prdEnumRoleType;

    @Value("${resendInviteEnabled}")
    private boolean resendInviteEnabled;

    @Value("${allowedStatus}")
    private String allowedOrganisationStatus;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

}
