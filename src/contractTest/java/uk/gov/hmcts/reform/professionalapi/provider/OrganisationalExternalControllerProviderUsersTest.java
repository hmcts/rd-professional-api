package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.WebMvcProviderTest;
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
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
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
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.ACTIVE;

@Provider("referenceData_organisationalExternalUsers")
@WebMvcTest({OrganisationExternalController.class})
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {OrganisationalExternalControllerProviderUsersTestConfiguration.class, WebConfig.class})
public class OrganisationalExternalControllerProviderUsersTest extends WebMvcProviderTest {

    private static final String ORGANISATION_EMAIL = "someemailaddress@organisation.com";
    private static final String USER_JWT = "Bearer some-access-token";
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

    @Autowired
    IdamRepository idamRepositoryMock;

    @Mock
    Authentication authentication;
    @Mock
    SecurityContext securityContext;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Organisation organisation;



}
