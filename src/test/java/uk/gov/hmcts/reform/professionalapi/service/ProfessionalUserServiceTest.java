package uk.gov.hmcts.reform.professionalapi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.LENGTH_OF_ORGANISATION_IDENTIFIER;
import static uk.gov.hmcts.reform.professionalapi.generator.ProfessionalApiGenerator.generateUniqueAlphanumericId;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;

import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserAttributeServiceImpl;

public class ProfessionalUserServiceTest {

    private final ProfessionalUserRepository professionalUserRepository = Mockito.mock(ProfessionalUserRepository.class);
    private final Organisation organisation = Mockito.mock(Organisation.class);
    private final OrganisationRepository organisationRepository = mock(OrganisationRepository.class);
    private final UserAttributeRepository userAttributeRepository = mock(UserAttributeRepository.class);
    private final PrdEnumRepository prdEnumRepository = mock(PrdEnumRepository.class);

    private final UserAttributeServiceImpl userAttributeService = mock(UserAttributeServiceImpl.class);

    private final ProfessionalUser professionalUser = new ProfessionalUser("some-fname",
            "some-lname",
            "SOME-EMAIL",
            ProfessionalUserStatus.PENDING,
            Mockito.mock(Organisation.class));

    private List<ProfessionalUser> usersNonEmptyList = new ArrayList<ProfessionalUser>();

    private final ProfessionalUserServiceImpl professionalUserService = new ProfessionalUserServiceImpl(
            organisationRepository, professionalUserRepository,
            userAttributeRepository, prdEnumRepository, userAttributeService);

    private NewUserCreationRequest newUserCreationRequest;

    private  List<PrdEnum> prdEnums;

    @Test
    public void retrieveUserByEmail() {
        Mockito.when(professionalUserRepository.findByEmailAddress(any(String.class)))
                .thenReturn(professionalUser);

        ProfessionalUser user = professionalUserService.findProfessionalUserByEmailAddress("some-email");
        assertEquals(professionalUser.getFirstName(), user.getFirstName());
        assertEquals(professionalUser.getLastName(), user.getLastName());
        assertEquals(professionalUser.getEmailAddress(), user.getEmailAddress());
    }

    public void retrieveUserByEmailNotFound() {
        Mockito.when(professionalUserRepository.findByEmailAddress(any(String.class)))
                .thenReturn(null);

        assertThat(professionalUserService.findProfessionalUserByEmailAddress("some-email")).isNull();
    }


    @Test
    public void findUsersByOrganisation_with_deleted_users() {

        usersNonEmptyList.add(professionalUser);
        Mockito.when(professionalUserRepository.findByOrganisation(organisation))
                .thenReturn(usersNonEmptyList);

        List<ProfessionalUser> usersFromDb = professionalUserService.findProfessionalUsersByOrganisation(organisation, true);
        Mockito.verify(
                professionalUserRepository,
                Mockito.times(1)).findByOrganisation(organisation);

        assertThat(usersFromDb).isNotNull();
    }

    @Test
    public void findUsersByOrganisation_with_non_deleted_users() {

        usersNonEmptyList.add(professionalUser);
        Mockito.when(professionalUserRepository.findByOrganisationAndStatusNot(organisation, ProfessionalUserStatus.DELETED))
                .thenReturn(usersNonEmptyList);

        List<ProfessionalUser> usersFromDb = professionalUserService.findProfessionalUsersByOrganisation(organisation, false);
        Mockito.verify(
                professionalUserRepository,
                Mockito.times(1)).findByOrganisationAndStatusNot(organisation, ProfessionalUserStatus.DELETED);

        assertThat(usersFromDb).isNotNull();
    }

    @Test
    public void addNewUserToAnOrganisation() {

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        newUserCreationRequest = new NewUserCreationRequest("first",
                "last",
                "DOMAIN@hotmail.com",
                "PENDING",
                userRoles);

        when(organisation.getOrganisationIdentifier()).thenReturn(generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER));
        when(organisationRepository.findByOrganisationIdentifier(organisation.getOrganisationIdentifier())).thenReturn(organisation);
        when(professionalUserRepository.save(any(ProfessionalUser.class))).thenReturn(professionalUser);

        NewUserResponse newUserResponse = professionalUserService.addNewUserToAnOrganisation(newUserCreationRequest, organisation.getOrganisationIdentifier());

        assertThat(newUserResponse).isNotNull();

        verify(organisationRepository, times(1)).findByOrganisationIdentifier(any(String.class));
        verify(professionalUserRepository, times(1)).save(any(ProfessionalUser.class));
        verify(organisation, times(1)).addProfessionalUser(any(ProfessionalUser.class));
        verify(userAttributeService, times(1)).addUserAttributesToUser(any(ProfessionalUser.class), (Mockito.anyList()));
    }

    @Test(expected = InvalidUseOfMatchersException.class)
    public void addNewUserWithInvalidFields() {
        when(professionalUserService.addNewUserToAnOrganisation(any(NewUserCreationRequest.class), any(String.class)))
                .thenReturn(null);

        professionalUserService.addNewUserToAnOrganisation(newUserCreationRequest, generateUniqueAlphanumericId(LENGTH_OF_ORGANISATION_IDENTIFIER));
    }
}