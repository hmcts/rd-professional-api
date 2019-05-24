package uk.gov.hmcts.reform.professionalapi.service;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;

import java.util.ArrayList;
import java.util.List;
import javax.xml.ws.http.HTTPException;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.professionalapi.domain.Organisation;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUser;
import uk.gov.hmcts.reform.professionalapi.domain.ProfessionalUserStatus;
import uk.gov.hmcts.reform.professionalapi.persistence.ProfessionalUserRepository;
import uk.gov.hmcts.reform.professionalapi.service.impl.ProfessionalUserServiceImpl;

public class ProfessionalUserServiceTest {

    private final ProfessionalUserRepository professionalUserRepository = Mockito.mock(ProfessionalUserRepository.class);
    private final Organisation organisation = Mockito.mock(Organisation.class);
    private final ProfessionalUser professionalUser = new ProfessionalUser("some-fname",
            "some-lname",
            "some-email",
            ProfessionalUserStatus.PENDING,
            Mockito.mock(Organisation.class));
    private final ProfessionalUserService professionalUserService = new ProfessionalUserServiceImpl(
            professionalUserRepository);
    private List<ProfessionalUser> usersNonEmptyList = new ArrayList<ProfessionalUser>();

    @Test
    public void retrieveUserByEmail() {
        Mockito.when(professionalUserRepository.findByEmailAddress(any(String.class)))
                .thenReturn(professionalUser);

        ProfessionalUser user = professionalUserService.findProfessionalUserByEmailAddress("some-email");
        assertEquals(professionalUser.getFirstName(), user.getFirstName());
        assertEquals(professionalUser.getLastName(), user.getLastName());
        assertEquals(professionalUser.getEmailAddress(), user.getEmailAddress());
    }

    @Test(expected = HTTPException.class)
    public void retrieveUserByEmailNotFound() {
        Mockito.when(professionalUserRepository.findByEmailAddress(any(String.class)))
                .thenReturn(null);

        professionalUserService.findProfessionalUserByEmailAddress("some-email");
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
}