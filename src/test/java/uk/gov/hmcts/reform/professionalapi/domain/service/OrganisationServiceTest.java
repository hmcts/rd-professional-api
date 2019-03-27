package uk.gov.hmcts.reform.professionalapi.domain.service;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.reform.professionalapi.domain.entities.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.entities.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.domain.service.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.OrganisationCreationRequest;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.request.UserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.infrastructure.controllers.response.OrganisationResponse;

public class OrganisationServiceTest {

    private final ProfessionalUserRepository professionalUserRepository = mock(ProfessionalUserRepository.class);
    private final OrganisationRepository organisationRepository = mock(OrganisationRepository.class);
    private final ProfessionalUser professionalUser = mock(ProfessionalUser.class);
    private final Organisation organisation = mock(Organisation.class);

    @Before
    public void setUp() {

        when(organisation.getId()).thenReturn(UUID.randomUUID());

        when(professionalUserRepository.save(any(ProfessionalUser.class)))
                .thenReturn(professionalUser);

        when(organisationRepository.save(any(Organisation.class)))
                .thenReturn(organisation);

    }

    @Test
    public void calls_repository_and_returns_an_organisation_response() {

        OrganisationService organisationService =
                new OrganisationService(organisationRepository, professionalUserRepository);

        UserCreationRequest superUser = new UserCreationRequest(
                "some-fname",
                "some-lname",
                "some-email");

        OrganisationCreationRequest organisationCreationRequest =
                new OrganisationCreationRequest("some-org-name", superUser);

        OrganisationResponse organisationResponse = organisationService.create(organisationCreationRequest);

        assertThat(organisationResponse).isNotNull();

        verify(
                organisationRepository,
                times(2)).save(any(Organisation.class));
    }
}