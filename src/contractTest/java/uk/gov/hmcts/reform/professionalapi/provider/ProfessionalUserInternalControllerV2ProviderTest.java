package uk.gov.hmcts.reform.professionalapi.provider;

import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import uk.gov.hmcts.reform.professionalapi.controller.internal.ProfessionalUserInternalControllerV2;
import uk.gov.hmcts.reform.professionalapi.controller.request.validator.impl.UsersInOrganisationsByOrganisationIdentifiersRequestValidatorImpl;
import uk.gov.hmcts.reform.professionalapi.domain.ContactInformation;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.OrganisationStatus;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.repository.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.OrganisationServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Provider("referenceData_professionalInternalUsersV2")
@Import(ProfessionalUserInternalControllerV2ProviderTestConfiguration.class)
public class ProfessionalUserInternalControllerV2ProviderTest extends MockMvcProviderTest {

    @Autowired
    ProfessionalUserRepository professionalUserRepository;

    @Autowired
    ProfessionalUserInternalControllerV2 professionalUserInternalControllerV2;

    @Autowired
    MappingJackson2HttpMessageConverter httpMessageConverter;

    @Autowired
    UsersInOrganisationsByOrganisationIdentifiersRequestValidatorImpl usersInOrgByIdentifierValidatorImpl;

    @Autowired
    OrganisationServiceImpl organisationService;

    @Autowired
    ProfessionalUserServiceImpl professionalUserService;

    public static final String ORG_NAME = "Org-Name";
    public static final String SRA_ID = "sra-id";
    public static final String COMPANY_NUMBER = "companyN";
    public static final String COMPANY_URL = "www.org.com";

    public static final String PBA_NUMBER = "PBA1234567";

    @Override
    void setController() {
        testTarget.setControllers(professionalUserInternalControllerV2);
        testTarget.setMessageConverters(httpMessageConverter);
    }

    @SuppressWarnings("unchecked")
    @State("A page size & list of organisation identifiers for a PRD internal organisation request")
    public void setUpOrganisationWithPageSize() {
        ProfessionalUser professionalUser = getProfessionalUser();
        Page<ProfessionalUser> professionalUserPage = (Page<ProfessionalUser>) mock(Page.class);

        when(professionalUserRepository.findUsersInOrganisationByOrganisationIdentifierAfterGivenUserAndAfterGivenOrganisation(anyList(), any(String.class), any(String.class), any(Pageable.class))).thenReturn(professionalUserPage);
        when(professionalUserPage.getContent()).thenReturn(List.of(professionalUser));
    }

    private ProfessionalUser getProfessionalUser() {
        ProfessionalUser professionalUser = new ProfessionalUser();
        professionalUser.setId(UUID.randomUUID());
        professionalUser.setFirstName("some name");
        professionalUser.setLastName("last name");
        professionalUser.setEmailAddress("test@email.com");
        professionalUser.setLastUpdated(LocalDateTime.now());

        professionalUser.setOrganisation(getOrganisation());

        return professionalUser;
    }

    private Organisation getOrganisation() {
        Organisation organisation = new Organisation(ORG_NAME, OrganisationStatus.PENDING, SRA_ID,
                COMPANY_NUMBER, false, COMPANY_URL);
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
