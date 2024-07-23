package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.professionalapi.WebMvcProviderTest;
import uk.gov.hmcts.reform.professionalapi.configuration.WebConfig;
import uk.gov.hmcts.reform.professionalapi.controller.internal.ProfessionalUserInternalController;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.pact.util.PactUtils.getMinimalOrganisation;
import static uk.gov.hmcts.reform.professionalapi.pact.util.PactUtils.getUserConfiguredAccesses;

@Provider("referenceData_professionalInternalUsers")
@WebMvcTest({ProfessionalUserInternalController.class})
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {ProfessionalUserInternalControllerProviderTestConfiguration.class, WebConfig.class})
public class ProfessionalUserInternalControllerProviderTest extends WebMvcProviderTest {

    @Autowired
    ProfessionalUserRepository professionalUserRepositoryMock;

    @State({"A user identifier for a PRD internal user request"})
    public void toRetrieveRefreshUserUsingUserIdentifier() {
        ProfessionalUser professionalUser = setupProfessionalUserWithOrganisation();

        when(professionalUserRepositoryMock.findByUserIdentifier(any(UUID.class)))
                .thenReturn(professionalUser);
    }

    @SuppressWarnings("unchecked")
    @State({"A since & page size for a PRD internal user request"})
    public void toRetrieveRefreshUserUsingSinceAndPagination() {
        ProfessionalUser professionalUser = setupProfessionalUserWithOrganisation();

        Page<ProfessionalUser> professionalUserPage = (Page<ProfessionalUser>) mock(Page.class);

        when(professionalUserRepositoryMock.findByLastUpdatedGreaterThanEqual(
                any(LocalDateTime.class), any(Pageable.class))
        ).thenReturn(professionalUserPage);
        when(professionalUserPage.getContent()).thenReturn(List.of(professionalUser));
    }

    @SuppressWarnings("unchecked")
    @State({"A since, searchAfter & page size for a PRD internal user request"})
    public void toRetrieveRefreshUserUsingSinceSearchAfterAndPagination() {
        ProfessionalUser professionalUser = setupProfessionalUserWithOrganisation();

        Page<ProfessionalUser> professionalUserPage = (Page<ProfessionalUser>) mock(Page.class);

        when(professionalUserRepositoryMock.findByLastUpdatedGreaterThanEqualAndIdGreaterThan(
                any(LocalDateTime.class), any(UUID.class), any(Pageable.class))
        ).thenReturn(professionalUserPage);
        when(professionalUserPage.getContent()).thenReturn(List.of(professionalUser));
    }

    private ProfessionalUser setupProfessionalUserWithOrganisation() {
        Organisation organisation = getMinimalOrganisation();
        organisation.setLastUpdated(LocalDateTime.now());

        ProfessionalUser professionalUser = new ProfessionalUser("firstName", "lastName",
                "email@org.com", organisation);

        professionalUser.setUserIdentifier("123");
        professionalUser.setId(UUID.randomUUID());
        professionalUser.setLastUpdated(LocalDateTime.now());
        professionalUser.setUserConfiguredAccesses(getUserConfiguredAccesses(professionalUser));

        return professionalUser;
    }
}
