package uk.gov.hmcts.reform.professionalapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Request;
import feign.Response;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.reform.professionalapi.controller.advice.ExternalApiException;
import uk.gov.hmcts.reform.professionalapi.controller.feign.UserProfileFeignClient;
import uk.gov.hmcts.reform.professionalapi.controller.response.OrganisationEntityResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.SuperUser;
import uk.gov.hmcts.reform.professionalapi.repository.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserProfileMatchClient.Profile;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus.ACTIVE;

class InternalOrganisationDetailTest {

    private static final String BEARER = "Bearer caller";
    private static final String ORG_ID = "O000001";
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
    private final UserProfileFeignClient legacyClient = mock(UserProfileFeignClient.class);
    private final ActiveOrganisationSearch search = mock(ActiveOrganisationSearch.class);
    private final OrganisationServiceImpl service = new OrganisationServiceImpl(search);
    private final Organisation org = new Organisation("Synthetic organisation", ACTIVE, null, null, false, null);

    InternalOrganisationDetailTest() {
        var repo = mock(OrganisationRepository.class);
        when(repo.findByOrganisationIdentifier(ORG_ID)).thenReturn(org);
        service.setOrganisationRepository(repo);
        service.setUserProfileFeignClient(legacyClient);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void displaysAnOrganisationWithNoAdministrator(boolean v2) {
        assertThat(mapper.valueToTree(detail(v2)).at("/superUser").isMissingNode()).isTrue();
        verifyNoInteractions(search, legacyClient);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void keepsStoredAdministratorWithAbsentOrBlankIdentifier(boolean v2) {
        var representative = addAdministrator(null);
        assertThat(firstName(detail(v2))).isEqualTo("Stored");
        representative.setUserIdentifier(" \t");
        assertThat(firstName(detail(v2))).isEqualTo("Stored");
        verifyNoInteractions(search, legacyClient);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void keepsStoredAdministratorOnExplicitAbsenceAndUsesCurrentSnapshotWhenPresent(boolean v2) {
        addAdministrator("user-id");
        when(search.profileForInternalDetail("user-id", BEARER))
                .thenReturn(null)
                .thenReturn(new Profile("user-id", "Current", "Profile", "current@example.test"));

        assertThat(firstName(detail(v2))).isEqualTo("Stored");
        assertThat(firstName(detail(v2))).isEqualTo("Current");
        assertThat(org.getUsers().getFirst().getFirstName()).isEqualTo("Stored");
        verifyNoInteractions(legacyClient);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void propagatesGenuineDependencyErrors(boolean v2) {
        addAdministrator("user-id");
        when(search.profileForInternalDetail("user-id", BEARER))
                .thenThrow(new ExternalApiException(BAD_GATEWAY, "User Profile search dependency failed"));

        assertThatThrownBy(() -> detail(v2)).isInstanceOfSatisfying(ExternalApiException.class,
                ex -> assertThat(ex.getHttpStatus()).isEqualTo(BAD_GATEWAY));
        verifyNoInteractions(legacyClient);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void externalDetailsKeepTheLegacyLookupAndItsStatus(boolean v2) {
        addAdministrator("user-id");
        when(legacyClient.getUserProfileById("user-id")).thenReturn(Response.builder()
                .request(mock(Request.class)).status(404).body("""
                        {"errorMessage":"No profile found"}
                        """, UTF_8).build());

        assertThatThrownBy(() -> {
            if (v2) {
                service.retrieveOrganisationForV2Api(ORG_ID, true);
            } else {
                service.retrieveOrganisation(ORG_ID, true);
            }
        }).isInstanceOfSatisfying(ExternalApiException.class,
                ex -> assertThat(ex.getHttpStatus()).isEqualTo(NOT_FOUND));
        verifyNoInteractions(search);
    }

    private OrganisationEntityResponse detail(boolean v2) {
        return v2 ? service.retrieveInternalOrganisationForV2Api(ORG_ID, BEARER)
                : service.retrieveInternalOrganisation(ORG_ID, BEARER);
    }

    private SuperUser addAdministrator(String userIdentifier) {
        var representative = new SuperUser("Stored", "Profile", "stored@example.test", org);
        representative.setUserIdentifier(userIdentifier);
        org.addProfessionalUser(representative);
        return representative;
    }

    private String firstName(OrganisationEntityResponse detail) {
        return mapper.valueToTree(detail).at("/superUser/firstName").asText();
    }
}
