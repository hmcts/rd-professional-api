package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.autoconfigure.DataJpaRepositoriesAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientAutoConfiguration;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.web.OAuth2ResourceServerWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
@ImportAutoConfiguration(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class,
    DataJpaRepositoriesAutoConfiguration.class,
    OAuth2ClientAutoConfiguration.class,
    OAuth2ClientWebSecurityAutoConfiguration.class,
    OAuth2ResourceServerAutoConfiguration.class,
    OAuth2ResourceServerWebSecurityAutoConfiguration.class
})
@Import({ProviderTestConfiguration.class, ProfessionalUserInternalControllerV2ProviderTestConfiguration.class,
    WebConfig.class})
public class ProfessionalUserInternalControllerV2ProviderTest extends WebMvcProviderTest {

    @Autowired
    ProfessionalUserRepository professionalUserRepository;

    private static final String ORG_NAME = "Org-Name";
    private static final String SRA_ID = "sra-id";
    private static final String COMPANY_NUMBER = "companyN";
    private static final String COMPANY_URL = "www.org.com";

    public static final String PBA_NUMBER = "PBA1234567";

    @SuppressWarnings("unchecked")
    @State("A page size, list of organisation identifiers and search after for a PRD internal user request")
    public void setUpOrganisationWithPageSizeAndSearchAfter() {
        ProfessionalUser professionalUser = getProfessionalUser();
        Page<ProfessionalUser> professionalUserPage = (Page<ProfessionalUser>) mock(Page.class);

        when(professionalUserRepository
                .findUsersInOrganisationsSearchAfter(anyList(),
                        any(UUID.class), any(UUID.class), any(Pageable.class))).thenReturn(professionalUserPage);
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
        professionalUser.setDeleted(null);
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
