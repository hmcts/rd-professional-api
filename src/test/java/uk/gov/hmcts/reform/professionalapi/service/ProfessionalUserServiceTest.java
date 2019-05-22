package uk.gov.hmcts.reform.professionalapi.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.xml.ws.http.HTTPException;

import static org.assertj.core.api.Java6Assertions.assertThat;
import org.junit.Test;

import org.mockito.Mockito;
import org.mockito.exceptions.misusing.InvalidUseOfMatchersException;
import uk.gov.hmcts.reform.professionalapi.controller.request.NewUserCreationRequest;
import uk.gov.hmcts.reform.professionalapi.controller.response.NewUserResponse;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.PrdEnum;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.persistence.OrganisationRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.PrdEnumRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.persistence.UserAttributeRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;
import uk.gov.hmcts.reform.professionalapi.service.impl.UserAttributeServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProfessionalUserServiceTest {

    private final ProfessionalUserRepository professionalUserRepository = mock(ProfessionalUserRepository.class);
    private final OrganisationRepository organisationRepository = mock(OrganisationRepository.class);
    private final UserAttributeRepository userAttributeRepository = mock(UserAttributeRepository.class);
    private final PrdEnumRepository prdEnumRepository = mock(PrdEnumRepository.class);

    private final Organisation organisation = mock(Organisation.class);

    private final UserAttributeServiceImpl userAttributeService = mock(UserAttributeServiceImpl.class);

    private final ProfessionalUser professionalUser = new ProfessionalUser("some-fname",
            "some-lname",
            "some-email",
            "PENDING",
            mock(Organisation.class));


    private final ProfessionalUserServiceImpl professionalUserService = new ProfessionalUserServiceImpl(
            organisationRepository, professionalUserRepository,
            userAttributeRepository, prdEnumRepository, userAttributeService);

    private NewUserCreationRequest newUserCreationRequest;

    private  List<PrdEnum> prdEnums;

    @Test
    public void retrieveUserByEmail() {
        when(professionalUserRepository.findByEmailAddress(any(String.class)))
                .thenReturn(professionalUser);

        ProfessionalUser user = professionalUserService.findProfessionalUserByEmailAddress("some-email");
        assertEquals(professionalUser.getFirstName(), user.getFirstName());
        assertEquals(professionalUser.getLastName(), user.getLastName());
        assertEquals(professionalUser.getEmailAddress(), user.getEmailAddress());
    }

    @Test(expected = HTTPException.class)
    public void retrieveUserByEmailNotFound() {
        when(professionalUserRepository.findByEmailAddress(any(String.class)))
                .thenReturn(null);

        professionalUserService.findProfessionalUserByEmailAddress("some-email");
    }

    @Test
    public void addNewUserToAnOrganisation() {

        List<String> userRoles = new ArrayList<>();
        userRoles.add("pui-user-manager");

        newUserCreationRequest = new NewUserCreationRequest("first",
                "last",
                "domain@hotmail.com",
                "PENDING",
                userRoles);

        when(organisation.getOrganisationIdentifier()).thenReturn(UUID.randomUUID());
        when(organisationRepository.findByOrganisationIdentifier(organisation.getOrganisationIdentifier())).thenReturn(organisation);
        when(professionalUserRepository.save(any(ProfessionalUser.class))).thenReturn(professionalUser);

        NewUserResponse newUserResponse = professionalUserService.addNewUserToAnOrganisation(newUserCreationRequest, organisation.getOrganisationIdentifier());

        assertThat(newUserResponse).isNotNull();

        verify(organisationRepository, times(1)).findByOrganisationIdentifier(any(UUID.class));
        verify(professionalUserRepository, times(1)).save(any(ProfessionalUser.class));
        verify(organisation, times(1)).addProfessionalUser(any(ProfessionalUser.class));
        verify(userAttributeService, times(1)).addUserAttributesToUser(any(ProfessionalUser.class), (Mockito.anyList()));
    }

    @Test(expected = InvalidUseOfMatchersException.class)
    public void addNewUserWithInvalidFields() {
        when(professionalUserService.addNewUserToAnOrganisation(any(NewUserCreationRequest.class), any(UUID.class)))
                .thenReturn(null);

        professionalUserService.addNewUserToAnOrganisation(newUserCreationRequest, UUID.randomUUID());
    }
}