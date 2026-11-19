package uk.gov.hmcts.reform.professionalapi.controller.external;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.professionalapi.configuration.resolver.OrganisationIdArgumentResolver;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.OrganisationIdentifierValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationMinimalInfoResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.repository.IdamRepository;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.OrganisationService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"PMD.UnitTestContainsTooManyAsserts", "PMD.UnitTestShouldIncludeAssert"})
class OrganisationMetadataExternalControllerTest {

    private static final String ORGANISATION_ID = "ORG1234";
    private static final String ORGANISATION_NAME = "Solicitors";
    private static final String USER_ID = "requesting-user";

    @Mock
    private OrganisationService organisationService;

    private OrganisationMetadataExternalController controller;

    @BeforeEach
    void setUp() {
        controller = new OrganisationMetadataExternalController(organisationService,
            new OrganisationIdentifierValidatorImpl(organisationService));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnOrganisationMetadataWithoutLoadingUsersOrAddresses() {
        Organisation organisation = spy(organisation(OrganisationStatus.ACTIVE));
        when(organisationService.getOrganisationByOrgIdentifier(ORGANISATION_ID)).thenReturn(organisation);

        OrganisationMinimalInfoResponse response = controller.getOrganisationMetadata(ORGANISATION_ID);

        assertThat(response.getOrganisationIdentifier()).isEqualTo(ORGANISATION_ID);
        assertThat(response.getName()).isEqualTo(ORGANISATION_NAME);
        assertThat(response.getContactInformation()).isNull();
        verify(organisation, never()).getUsers();
        verify(organisation, never()).getContactInformation();
    }

    @Test
    void shouldRejectMissingOrganisation() {
        assertThrows(EmptyResultDataAccessException.class, () -> controller.getOrganisationMetadata(ORGANISATION_ID));
    }

    @Test
    void shouldRejectInactiveOrganisation() {
        Organisation organisation = organisation(OrganisationStatus.PENDING);
        when(organisationService.getOrganisationByOrgIdentifier(ORGANISATION_ID)).thenReturn(organisation);

        assertThrows(EmptyResultDataAccessException.class, () -> controller.getOrganisationMetadata(ORGANISATION_ID));
    }

    @Test
    void shouldAllowCaseAssignmentRole() {
        Organisation organisation = organisation(OrganisationStatus.ACTIVE);
        when(organisationService.getOrganisationByOrgIdentifier(ORGANISATION_ID)).thenReturn(organisation);
        authenticate("pui-caa");

        try (AnnotationConfigApplicationContext context = securedContext()) {
            assertThat(context.getBean(OrganisationMetadataExternalController.class)
                .getOrganisationMetadata(ORGANISATION_ID).getOrganisationIdentifier()).isEqualTo(ORGANISATION_ID);
        }
    }

    @Test
    void shouldRejectUnauthorisedRoleBeforeLookingUpOrganisation() {
        authenticate("citizen");

        try (AnnotationConfigApplicationContext context = securedContext()) {
            OrganisationMetadataExternalController securedController =
                context.getBean(OrganisationMetadataExternalController.class);
            assertThrows(AccessDeniedException.class, () -> securedController.getOrganisationMetadata(ORGANISATION_ID));
        }
        verifyNoInteractions(organisationService);
    }

    @Test
    void shouldResolveOrganisationFromAuthenticatedUserAndReturnOnlyMetadata() throws Exception {
        Organisation organisation = organisation(OrganisationStatus.ACTIVE);
        when(organisationService.getOrganisationByOrgIdentifier(ORGANISATION_ID)).thenReturn(organisation);
        ProfessionalUserRepository userRepository = mock(ProfessionalUserRepository.class);
        IdamRepository idamRepository = mock(IdamRepository.class);
        UserInfo userInfo = mock(UserInfo.class);
        when(userInfo.getUid()).thenReturn(USER_ID);
        when(idamRepository.getUserInfo("user-token")).thenReturn(userInfo);
        when(userRepository.findByUserIdentifier(USER_ID)).thenReturn(
            new ProfessionalUser("First", "Last", "user@example.com", organisation));
        OrganisationIdArgumentResolver resolver = new OrganisationIdArgumentResolver();
        ReflectionTestUtils.setField(resolver, "professionalUserRepository", userRepository);
        ReflectionTestUtils.setField(resolver, "idamRepository", idamRepository);
        Jwt jwt = Jwt.withTokenValue("user-token").header("alg", "none").subject(USER_ID).build();
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
            jwt, "unused", List.of(new SimpleGrantedAuthority("pui-caa"))));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).setCustomArgumentResolvers(resolver).build();

        mockMvc.perform(get("/refdata/external/v1/organisations/metadata")
                .param("organisationIdentifier", "ANOTHER_ORGANISATION"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.organisationIdentifier").value(ORGANISATION_ID))
            .andExpect(jsonPath("$.name").value(ORGANISATION_NAME))
            .andExpect(jsonPath("$.users").doesNotExist())
            .andExpect(jsonPath("$.contactInformation").doesNotExist());
    }

    private Organisation organisation(OrganisationStatus status) {
        Organisation organisation = new Organisation(ORGANISATION_NAME, status,
            "sra-id", "company-number", false, "https://example.com");
        organisation.setOrganisationIdentifier(ORGANISATION_ID);
        return organisation;
    }

    private void authenticate(String role) {
        SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
            USER_ID, "unused", List.of(new SimpleGrantedAuthority(role))));
    }

    private AnnotationConfigApplicationContext securedContext() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(MethodSecurityConfiguration.class);
        context.registerBean(OrganisationService.class, () -> organisationService);
        context.registerBean(OrganisationIdentifierValidatorImpl.class,
            () -> new OrganisationIdentifierValidatorImpl(organisationService));
        context.registerBean(OrganisationMetadataExternalController.class);
        context.refresh();
        return context;
    }

    @Configuration(proxyBeanMethods = false)
    @EnableMethodSecurity(securedEnabled = true)
    static class MethodSecurityConfiguration {
    }
}
