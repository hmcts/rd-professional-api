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
import uk.gov.hmcts.reform.professionalapi.controller.internal.ProfessionalUserInternalControllerV2;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.pact.util.PactUtils.getUserConfiguredAccesses;

@Provider("referenceData_professionalInternalUsersV2")
@WebMvcTest({ProfessionalUserInternalControllerV2.class})
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {ProfessionalUserInternalControllerV2ProviderTestConfiguration.class, WebConfig.class})
public class ProfessionalUserInternalControllerV2ProviderTest extends WebMvcProviderTest {

    @Autowired
    ProfessionalUserRepository professionalUserRepository;

    public static final String ORG_NAME = "Org-Name";
    public static final String SRA_ID = "sra-id";
    public static final String COMPANY_NUMBER = "companyN";
    public static final String COMPANY_URL = "www.org.com";

    public static final String PBA_NUMBER = "PBA1234567";

    @SuppressWarnings("unchecked")
    @State("A page size, list of organisation identifiers and search after for a PRD internal user request")
    public void setUpOrganisationWithPageSizeAndSearchAfter() {
        ProfessionalUser professionalUser = getProfessionalUser();
        Page<ProfessionalUser> professionalUserPage = (Page<ProfessionalUser>) mock(Page.class);

        when(professionalUserRepository
                .findUsersInOrganisationsSearchAfter(anyList(),
                        any(String.class), any(String.class), any(Pageable.class))).thenReturn(professionalUserPage);
        when(professionalUserPage.getContent()).thenReturn(List.of(professionalUser));
    }

    @SuppressWarnings("unchecked")
    @State("A page size, list of organisation identifiers and no search after for a PRD internal user request")
    public void setUpOrganisationWithPageSizeAndNoSearchAfter() {
        ProfessionalUser professionalUser = getProfessionalUser();
        Page<ProfessionalUser> professionalUserPage = (Page<ProfessionalUser>) mock(Page.class);

        when(professionalUserRepository.findUsersInOrganisations(anyList(),
                any(Pageable.class))).thenReturn(professionalUserPage);
        when(professionalUserPage.getContent()).thenReturn(List.of(professionalUser));
    }

    private ProfessionalUser getProfessionalUser() {
        ProfessionalUser professionalUser = new ProfessionalUser();
        professionalUser.setId(UUID.randomUUID());
        professionalUser.setUserIdentifier(UUID.randomUUID().toString());
        professionalUser.setFirstName("some name");
        professionalUser.setLastName("last name");
        professionalUser.setEmailAddress("test@email.com");
        professionalUser.setLastUpdated(LocalDateTime.now());

        professionalUser.setOrganisation(getOrganisation());

        professionalUser.setUserConfiguredAccesses(getUserConfiguredAccesses(professionalUser));

        return professionalUser;
    }

    private Organisation getOrganisation() {
        Organisation organisation = new Organisation(ORG_NAME, OrganisationStatus.PENDING, SRA_ID,
                COMPANY_NUMBER, false, COMPANY_URL);
        organisation.setId(UUID.randomUUID());
        organisation.setSraRegulated(true);
        organisation.setOrganisationIdentifier("someOrganisationIdentifier");
        ContactInformation contactInformation = new ContactInformation();
        contactInformation.setUprn("uprn");
        contactInformation.setAddressLine1("addressLine1");
        contactInformation.setAddressLine2("addressLine2");
        contactInformation.setCountry("country");
        contactInformation.setPostCode("HA5 1BJ");
        contactInformation.setCreated(LocalDateTime.now());
        contactInformation.setId(UUID.randomUUID());
        organisation.setContactInformations(List.of(contactInformation));
        return organisation;
    }
}
