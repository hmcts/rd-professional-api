package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.professionalapi.WebMvcProviderTest;
import uk.gov.hmcts.reform.professionalapi.configuration.WebConfig;
import uk.gov.hmcts.reform.professionalapi.controller.internal.ProfessionalUserInternalController;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.UserConfiguredAccess;
import uk.gov.hmcts.reform.professionalapi.domain.UserConfiguredAccessId;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.pact.util.PactUtils.getMinimalOrganisation;

@Provider("referenceData_professionalInternalUsers")
@WebMvcTest({ProfessionalUserInternalController.class})
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {ProfessionalUserInternalControllerProviderTestConfiguration.class, WebConfig.class})
public class ProfessionalUserInternalControllerProviderTest extends WebMvcProviderTest {

    @Autowired
    ProfessionalUserRepository professionalUserRepositoryMock;

    @State({"A user identifier for a PRD internal user request"})
    public void toRetrieveRefreshUser() {
        ProfessionalUser professionalUser = new ProfessionalUser("firstName", "lastName",
                "email@org.com", getMinimalOrganisation());

        UserConfiguredAccessId userConfiguredAccessId = new UserConfiguredAccessId(
                professionalUser,
                "CIVIL",
                "SOLICITOR_PROFILE",
                "123"
        );

        UserConfiguredAccess userConfiguredAccess = new UserConfiguredAccess(
                userConfiguredAccessId, true
        );

        professionalUser.setUserConfiguredAccesses(List.of(userConfiguredAccess));

        when(professionalUserRepositoryMock.findByUserIdentifier(any(String.class)))
                .thenReturn(professionalUser);
    }

}
