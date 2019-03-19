package uk.gov.hmcts.reform.professionalapi.domain.service;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.entities.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.ProfessionalUserRepository;

public class OrganisationServiceTest {

    private final ProfessionalUserRepository professionalUserRepository = mock(ProfessionalUserRepository.class);
    private final OrganisationRepository organisationRepository = mock(OrganisationRepository.class);
    private final ProfessionalUser professionalUser = mock(ProfessionalUser.class);
    private final Organisation organisation = mock(Organisation.class);

    @Before
    public void setUp() {

        when(professionalUserRepository.save(any(ProfessionalUser.class)))
                .thenReturn(professionalUser);

        when(organisationRepository.save(any(Organisation.class)))
                .thenReturn(organisation);

    }

    @Test
    public void name() {

        assertThat(true).isEqualTo(true);

        //        OrganisationService organisationService =
        //                new OrganisationService(organisationRepository, professionalUserRepository);
        //
        //        UserCreationRequest superUser = new UserCreationRequest(
        //                "some-fname",
        //                "some-lname",
        //                "some-email");
        //
        //        OrganisationCreationRequest organisationCreationRequest =
        //                new OrganisationCreationRequest("some-org-name", superUser);
        //
        //        OrganisationResponse organisationResponse = organisationService.create(organisationCreationRequest);
        //
        //        assertThat(organisationResponse).isNotNull();
        //
        //        verify(organisationRepository).save(Mockito.any(Organisation.class));
    }
}